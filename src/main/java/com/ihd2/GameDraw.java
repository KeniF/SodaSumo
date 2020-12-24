package com.ihd2;

import com.ihd2.model.Mass;
import com.ihd2.model.Model;
import com.ihd2.model.Spring;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

public class GameDraw extends JComponent { //so it can be added like a "window"
    private static final int FRAME_DELAY = 20; //ms 30ms~33.33fps
    private static final double GROUND_HEIGHT = 0.0;
    private static final double SURFACE_FRICTION = 0.1;
    private static final double SURFACE_REFLECTION = -0.75;// x velocity left when hitting ground
    private static final double MODEL_REFLECTION = -0.75;//y velocity left when hitting ground
    private static final double SPEED_LIMIT = 10.0;//solves model explosion problem!!
    private static final double ENERGY_LEFT = 0.95;
    private static final RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
    private static final Font font = new Font("Arial", Font.PLAIN, 12);
    private static final Font resultFont = new Font("Arial", Font.PLAIN, 20);
    private static final double MASS_SIZE = 4.0;
    private static final float LINE_WIDTH = 0.4f;
    private static final double SHIFT = MASS_SIZE / 2.0; //Shift needed because specified point is ellipse's top-left
    private final double HEIGHT = 298.0;//need to invert height as top-left is (0,0)
    private final Line2D.Double horizontalLine = new Line2D.Double();
    private final Line2D.Double lineOld = new Line2D.Double();
    private final Line2D.Double lineNew = new Line2D.Double();
    private final Line2D.Double massLine = new Line2D.Double();
    private final Ellipse2D.Double g2dEllipse = new Ellipse2D.Double();
    private final Line2D.Double g2dLine = new Line2D.Double();
    private double TIME_LIMIT_MS = 15000.0;
    private Thread testThread;
    private double noOfFrames1 = 0.0;
    private double noOfFrames2 = 0.0;
    private double gameFrames = 0.0;
    private Model model1, model2;
    private Graphics2D gfx2d;
    private double[][] m1Acceleration, m2Acceleration;
    private static volatile boolean run = false;
    private volatile boolean invertM1 = false, invertM2 = false;
    private double firstContactPoint = 0;
    private boolean touched = false;
    private String resultMessage = "";

    public void invertM1() {
        invertM1 = !invertM1;
    }

    public void invertM2() {
        invertM2 = !invertM2;
    }

    public void paint(Graphics g) {
        gfx2d = (Graphics2D) g;
        BasicStroke bs = new BasicStroke(LINE_WIDTH);
        gfx2d.setStroke(bs);
        gfx2d.setColor(Color.BLACK);//color of pen
        gfx2d.setRenderingHints(rh);//turns on anti-aliasing
        if (model1 != null)
            drawModel(model1);
        if (model2 != null)
            drawModel(model2);
        if (touched)
            drawVerticalLine();
        if (!resultMessage.equals(""))
            drawResult();
    }

    public void pause() {
        run = false;
    }

    public void setTimeLimit(double milliseconds) {
        TIME_LIMIT_MS = milliseconds;
    }

    private void drawVerticalLine() {
        gfx2d.setColor(Color.GRAY);
        g2dLine.setLine(firstContactPoint, HEIGHT + 10.0, firstContactPoint, HEIGHT - 1000.0);
        gfx2d.draw(g2dLine);
        gfx2d.setFont(font);
    }

    private void drawResult() {
        gfx2d.setColor(Color.BLUE.darker());
        gfx2d.setFont(resultFont);
        gfx2d.drawString(resultMessage, (int) Sodasumo.GAME_WIDTH / 2 - 150, 50);
    }

    private void drawModel(Model model) {
        drawMasses(model);
        drawSprings(model);
        drawMuscles(model);
    }

    private void drawMasses(final Model model) {
        for (final Mass mass : model.getMassMap().values()) {
            g2dEllipse.setFrame(mass.getX() - SHIFT,
                    HEIGHT - (mass.getY() + SHIFT), MASS_SIZE, MASS_SIZE);
            gfx2d.fill(g2dEllipse);
        }
    }

    private void drawSprings(final Model model) {
        for (final Spring spring: model.getSpringMap().values()) {
            final Mass mass1 = spring.getMass1();
            final Mass mass2 = spring.getMass2();
            g2dLine.setLine(mass1.getX(), HEIGHT - mass1.getY(),
                    mass2.getX(), HEIGHT - mass2.getY());
            gfx2d.draw(g2dLine);
        }
    }

    private void drawMuscles(final Model model) {
        for (final Spring muscle: model.getMuscleMap().values()) {
            final Mass mass1 = muscle.getMass1();
            final Mass mass2 = muscle.getMass2();
            double dMass1X = mass1.getX();
            double dMass1Y = mass1.getY();
            double dMass2X = mass2.getX();
            double dMass2Y = mass2.getY();
            g2dLine.setLine(dMass1X, HEIGHT - dMass1Y,
                    dMass2X, HEIGHT - dMass2Y);
            gfx2d.draw(g2dLine);
            g2dEllipse.setFrame((dMass1X + dMass2X) / 2.0 - 1.5,
                    HEIGHT - ((dMass1Y + dMass2Y) / 2.0 + 1.5), 3.0, 3.0);
            gfx2d.fill(g2dEllipse);
        }
    }

    public void init() {
        run = true;
        noOfFrames1 = 0.0;
        noOfFrames2 = 0.0;
        gameFrames = 0.0;
        m1Acceleration = new double[model1.getMassMap().size()][2];
        m2Acceleration = new double[model2.getMassMap().size()][2];
        touched = false;
        resultMessage = "";
    }

    public void provideModel1(Model model) {
        run = false;
        model1 = null;
        model1 = model;
        double[] br = model1.getBoundingRectangle();
        double shiftRight = Sodasumo.GAME_WIDTH / 2.0 - br[3] - 10.0;//-Math.random()*10;
        for (int i = 1; i <= model1.getMassMap().size(); i++) {
            model1.getMass(i).setX(model1.getMass(i).getX() + shiftRight);
        }
    }

    public void provideModel2(Model model) {
        run = false;
        model2 = null;
        model2 = model;
        double[] br = model2.getBoundingRectangle();
        double shiftRight = Sodasumo.GAME_WIDTH / 2.0 - br[2] + 10.0;//+Math.random()*10;
        for (int i = 1; i <= model2.getMassMap().size(); i++) {
            model2.getMass(i).setX(model2.getMass(i).getX() + shiftRight);
        }
    }

    public void startDraw() {
        testThread = new Thread(() -> {
            Thread curThread = Thread.currentThread();
            long beforeRun = System.currentTimeMillis();
            while (curThread == testThread) {
                if (run) {
                    if (gameFrames > TIME_LIMIT_MS / FRAME_DELAY) {
                        gameEnds();
                    } else {
                        physics();
                        repaint();
                        gameFrames += 1.0;
                        if (!invertM1)
                            noOfFrames1 += 1.0;
                        else noOfFrames1 -= 1.0;
                        if (!invertM2)
                            noOfFrames2 += 1.0;
                        else noOfFrames2 -= 1.0;
                    }
                }
                try {
                    //to keep a constant framerate depending on how far we are behind :D
                    beforeRun += FRAME_DELAY;
                    Thread.sleep(Math.max(0, beforeRun - System.currentTimeMillis()));
                } catch (InterruptedException e) {
                    System.out.println(e);
                }
            }
        });
        testThread.start();
    }

    //happens when 1 model gets pushed out of the ring / timeout
    private void gameEnds() {//returns integer value telling result of model1
        run = false;
        resultMessage(currentScore());
        repaint();
    }

    private String resultMessage(int score) {
        if (score == 0) {
            resultMessage = "Draw - They are equally good!";
        } else if (score > 0) {
            resultMessage = model1.getName() + " wins! Score:" + score;
        } else {
            resultMessage = model2.getName() + " wins! Score:" + (-score);
        }
        return resultMessage;
    }

    private int currentScore() {
        return (int) ((model1.getBoundRight() - firstContactPoint +
                model2.getBoundLeft() - firstContactPoint) / 20);
    }

    private void physics() {
        if (isRunnable()) {
            //create an acceleration array for all masses
            //adds up all accleration to calculate new velocity hence position
            //java automatically sets array values to 0.0
            m1Acceleration = new double[model1.getMassMap().size()][2];
            m2Acceleration = new double[model2.getMassMap().size()][2];
            accelerateSprings(model1, 1, false);
            accelerateSprings(model1, 1, true);
            newPositions(1);
            accelerateSprings(model2, 2, true);
            accelerateSprings(model2, 2, false);
            newPositions(2);
            doCollision();
        }
    }

    private boolean isRunnable() {
        //to be extended to include run button
        boolean b = false;
        if (model1 != null & model2 != null)
            b = true;
        return b;
    }

    private void accelerateSprings(Model m, int input, boolean isMuscle) {
        int totalNo;
        if (isMuscle)
            totalNo = m.getMuscleMap().size();
        else
            totalNo = m.getSpringMap().size();
        for (int i = 1; i <= totalNo; i++) {
            double newRLength;
            double mass1Y;
            double mass1X;
            double mass2X;
            double mass2Y;
            int loadMass1;
            int loadMass2;
            if (isMuscle) {
                double amp = Math.abs(m.getMuscle(i).getAmplitude());
                double phase = m.getMuscle(i).getPhase();
                double rLength = m.getMuscle(i).getRestLength();
                //new=old*(1.0+ waveAmplitude*muscleAmplitude*sine())
                //*2 pi to convert to radians | -wavePhase to set correct restLength of com.ihd2.model.Muscle
                //don't do the period already done
                //One for each model, allows reversing
                final double noOfFrames = (input == 1) ? noOfFrames1 : noOfFrames2;
                newRLength = rLength * (1.0 + m.getWaveAmplitude() * amp *
                        Math.sin((m.getWaveSpeed() * noOfFrames + phase - m.getWavePhase()) * 2.0 * Math.PI));
                mass1X = m.getMuscle(i).getMass1().getX();
                mass1Y = m.getMuscle(i).getMass1().getY();
                mass2X = m.getMuscle(i).getMass2().getX();
                mass2Y = m.getMuscle(i).getMass2().getY();
                loadMass1 = m.getMuscle(i).getMass1().getId();
                loadMass2 = m.getMuscle(i).getMass2().getId();
            } else {
                newRLength = m.getSpring(i).getRestLength();
                mass1X = m.getSpring(i).getMass1().getX();
                mass1Y = m.getSpring(i).getMass1().getY();
                mass2X = m.getSpring(i).getMass2().getX();
                mass2Y = m.getSpring(i).getMass2().getY();
                loadMass1 = m.getSpring(i).getMass1().getId();
                loadMass2 = m.getSpring(i).getMass2().getId();
            }

            double lengthX = Math.abs(mass1X - mass2X);//absolute value, so angle is always +
            double lengthY = Math.abs(mass1Y - mass2Y);
            double length = Math.sqrt(lengthX * lengthX + lengthY * lengthY); //Pythagoras'
            double extension = (length - newRLength);
            //Frictional force affects velocity only!!
            double resultantAcceleration = Math.abs(m.getSpringyness() * extension);//F=kx=ma where m=1.0
            //gets the masses connected to the current muscle
            //Mass#109 --> 109
            double angle = Math.atan(lengthY / lengthX);//in radians
            if (extension != 0.0) {//if springs are pulled/pushed
                //if extension>0.0, spring pulled, resultantAcceleration is correct
                //if extension<0.0, spring pushed, resultantAcceleration needs to be inverted
                if (extension < 0.0)
                    resultantAcceleration *= -1;
                if (mass1X > mass2X) {
                    if (mass2Y > mass1Y) {
                        if (input == 1) {
                            m1Acceleration[loadMass1 - 1][0] -= resultantAcceleration * Math.cos(angle);
                            m1Acceleration[loadMass1 - 1][1] += resultantAcceleration * Math.sin(angle);
                            m1Acceleration[loadMass2 - 1][0] += resultantAcceleration * Math.cos(angle);
                            m1Acceleration[loadMass2 - 1][1] -= resultantAcceleration * Math.sin(angle);
                        } else {
                            m2Acceleration[loadMass1 - 1][0] -= resultantAcceleration * Math.cos(angle);
                            m2Acceleration[loadMass1 - 1][1] += resultantAcceleration * Math.sin(angle);
                            m2Acceleration[loadMass2 - 1][0] += resultantAcceleration * Math.cos(angle);
                            m2Acceleration[loadMass2 - 1][1] -= resultantAcceleration * Math.sin(angle);
                        }
                    } else if (mass2Y < mass1Y) {
                        if (input == 1) {
                            m1Acceleration[loadMass1 - 1][0] -= resultantAcceleration * Math.cos(angle);
                            m1Acceleration[loadMass1 - 1][1] -= resultantAcceleration * Math.sin(angle);
                            m1Acceleration[loadMass2 - 1][0] += resultantAcceleration * Math.cos(angle);
                            m1Acceleration[loadMass2 - 1][1] += resultantAcceleration * Math.sin(angle);
                        } else {
                            m2Acceleration[loadMass1 - 1][0] -= resultantAcceleration * Math.cos(angle);
                            m2Acceleration[loadMass1 - 1][1] -= resultantAcceleration * Math.sin(angle);
                            m2Acceleration[loadMass2 - 1][0] += resultantAcceleration * Math.cos(angle);
                            m2Acceleration[loadMass2 - 1][1] += resultantAcceleration * Math.sin(angle);
                        }
                    } else {
                        if (input == 1) {
                            m1Acceleration[loadMass1 - 1][0] -= resultantAcceleration;
                            m1Acceleration[loadMass2 - 1][0] += resultantAcceleration;
                        } else {
                            m2Acceleration[loadMass1 - 1][0] -= resultantAcceleration;
                            m2Acceleration[loadMass2 - 1][0] += resultantAcceleration;
                        }
                    }
                } else if (mass1X < mass2X) {
                    if (mass2Y > mass1Y) {
                        if (input == 1) {//model1
                            m1Acceleration[loadMass1 - 1][0] += resultantAcceleration * Math.cos(angle);
                            m1Acceleration[loadMass1 - 1][1] += resultantAcceleration * Math.sin(angle);
                            m1Acceleration[loadMass2 - 1][0] -= resultantAcceleration * Math.cos(angle);
                            m1Acceleration[loadMass2 - 1][1] -= resultantAcceleration * Math.sin(angle);
                        } else {
                            m2Acceleration[loadMass1 - 1][0] += resultantAcceleration * Math.cos(angle);
                            m2Acceleration[loadMass1 - 1][1] += resultantAcceleration * Math.sin(angle);
                            m2Acceleration[loadMass2 - 1][0] -= resultantAcceleration * Math.cos(angle);
                            m2Acceleration[loadMass2 - 1][1] -= resultantAcceleration * Math.sin(angle);
                        }
                    } else if (mass2Y < mass1Y) {
                        if (input == 1) {
                            m1Acceleration[loadMass1 - 1][0] += resultantAcceleration * Math.cos(angle);
                            m1Acceleration[loadMass1 - 1][1] -= resultantAcceleration * Math.sin(angle);
                            m1Acceleration[loadMass2 - 1][0] -= resultantAcceleration * Math.cos(angle);
                            m1Acceleration[loadMass2 - 1][1] += resultantAcceleration * Math.sin(angle);
                        } else {
                            m2Acceleration[loadMass1 - 1][0] += resultantAcceleration * Math.cos(angle);
                            m2Acceleration[loadMass1 - 1][1] -= resultantAcceleration * Math.sin(angle);
                            m2Acceleration[loadMass2 - 1][0] -= resultantAcceleration * Math.cos(angle);
                            m2Acceleration[loadMass2 - 1][1] += resultantAcceleration * Math.sin(angle);
                        }
                    } else {
                        if (input == 1) {
                            m1Acceleration[loadMass1 - 1][0] += resultantAcceleration;//x
                            m1Acceleration[loadMass2 - 1][0] -= resultantAcceleration;//x
                        } else {
                            m2Acceleration[loadMass1 - 1][0] += resultantAcceleration;//x
                            m2Acceleration[loadMass2 - 1][0] -= resultantAcceleration;//x
                        }
                    }
                } else {
                    if (mass1Y > mass2Y) {
                        if (input == 1) {
                            m1Acceleration[loadMass1 - 1][1] -= resultantAcceleration;//y
                            m1Acceleration[loadMass2 - 1][1] += resultantAcceleration;
                        } else {
                            m2Acceleration[loadMass1 - 1][1] -= resultantAcceleration;//y
                            m2Acceleration[loadMass2 - 1][1] += resultantAcceleration;
                        }
                    } else if (mass1Y < mass2Y) {
                        if (input == 1) {
                            m1Acceleration[loadMass1 - 1][1] += resultantAcceleration;
                            m1Acceleration[loadMass2 - 1][1] -= resultantAcceleration;
                        } else {
                            m2Acceleration[loadMass1 - 1][1] += resultantAcceleration;
                            m2Acceleration[loadMass2 - 1][1] -= resultantAcceleration;
                        }
                    } else {
                        System.out.println("Error: Spring without extension!");
                    }
                }
            }
        }
    }

    private void newPositions(int model) {
        double newVelocityX;
        double newVelocityY;
        double newPositionX;
        double newPositionY;
        double oldVelocityY;
        double oldVelocityX;
        double oldPositionX;
        double oldPositionY;
        if (model == 1) {
            model1.resetBoundRect();
            for (int i = 1; i <= model1.getMassMap().size(); i++) {
                //damping for F=-fv
                oldVelocityX = model1.getMass(i).getVx();
                oldVelocityY = model1.getMass(i).getVy();
                newVelocityX = oldVelocityX + m1Acceleration[i - 1][0];
                newVelocityX = newVelocityX - newVelocityX * model1.getFriction();
                newVelocityY = oldVelocityY + m1Acceleration[i - 1][1];//-model1.getGravity();
                newVelocityY = newVelocityY - newVelocityY * model1.getFriction();
                newVelocityY -= model1.getGravity();//gravity(not damped!)
                if (Math.abs(newVelocityY) > SPEED_LIMIT) {
                    if (newVelocityY > 0)
                        newVelocityY = SPEED_LIMIT;
                    else newVelocityY = 0 - SPEED_LIMIT;
                }
                if (Math.abs(newVelocityX) > SPEED_LIMIT) {
                    if (newVelocityX > 0)
                        newVelocityX = SPEED_LIMIT;
                    else newVelocityX = 0 - SPEED_LIMIT;
                }

                oldPositionX = model1.getMass(i).getX();
                oldPositionY = model1.getMass(i).getY();
                newPositionX = oldPositionX + newVelocityX;
                newPositionY = oldPositionY + newVelocityY;

                //if goes through ground
                if (newPositionY <= GROUND_HEIGHT) {
                    if (newVelocityY < 0)
                        newVelocityY = newVelocityY * SURFACE_REFLECTION;
                    newPositionY = GROUND_HEIGHT;
                    newVelocityX *= SURFACE_FRICTION;
                }
                Mass model1Mass = model1.getMass(i);
                model1Mass.setVx(newVelocityX);
                model1Mass.setVy(newVelocityY);
                model1Mass.setX(newPositionX);
                model1Mass.setY(newPositionY);
                model1.adjustBoundRect(model1Mass);
            }
        }
        if (model == 2) {
            model2.resetBoundRect();
            for (int i = 1; i <= model2.getMassMap().size(); i++) {
                //damping for F=-fv
                oldVelocityX = model2.getMass(i).getVx();
                oldVelocityY = model2.getMass(i).getVy();
                newVelocityX = oldVelocityX + m2Acceleration[i - 1][0];
                newVelocityX = newVelocityX - newVelocityX * model2.getFriction();
                newVelocityY = oldVelocityY + m2Acceleration[i - 1][1];
                newVelocityY = newVelocityY - newVelocityY * model2.getFriction();
                newVelocityY -= model2.getGravity();
                if (Math.abs(newVelocityY) > SPEED_LIMIT) {
                    if (newVelocityY > 0)
                        newVelocityY = SPEED_LIMIT;
                    else newVelocityY = 0 - SPEED_LIMIT;
                }
                if (Math.abs(newVelocityX) > SPEED_LIMIT) {
                    if (newVelocityX > 0)
                        newVelocityX = SPEED_LIMIT;
                    else newVelocityX = 0 - SPEED_LIMIT;
                }
                oldPositionX = model2.getMass(i).getX();
                oldPositionY = model2.getMass(i).getY();
                newPositionX = oldPositionX + newVelocityX;
                newPositionY = oldPositionY + newVelocityY;

                //if goes through ground
                if (newPositionY <= GROUND_HEIGHT) {
                    if (newVelocityY < 0)
                        newVelocityY = newVelocityY * SURFACE_REFLECTION;
                    newPositionY = GROUND_HEIGHT;
                    newVelocityX *= SURFACE_FRICTION;
                }
                Mass model2Mass = model2.getMass(i);
                model2Mass.setVx(newVelocityX);
                model2Mass.setVy(newVelocityY);
                model2Mass.setX(newPositionX);
                model2Mass.setY(newPositionY);
                model2.adjustBoundRect(model2Mass);
            }
        }
    }

    private void doCollision() {
        if (model1.getBoundRight() > model2.getBoundLeft()) {
            //go through springs and muscles of model2
            double cmass1X;
            double cmass2X;
            double cmass1Y;
            double cmass2Y;
            double kineticEnergyX1;
            double kineticEnergyY1;
            double slopeOfLine;
            double yIntercept;
            double currentMassX;
            double currentMassY;
            double currentMassOldX;
            double currentMassOldY;
            double currentMassVx;
            double currentMassVy;
            int resultOld;
            int resultNew;
            Mass currentMass;
            Mass cmass1;
            Mass cmass2;
            for (int j = 1; j <= model1.getMassMap().size(); j++) {
                currentMass = model1.getMass(j);
                currentMassX = currentMass.getX();
                currentMassY = currentMass.getY();
                for (int i = 1; i <= model2.getSpringMap().size(); i++) {
                    cmass1 = model2.getSpring(i).getMass1();
                    cmass2 = model2.getSpring(i).getMass2();
                    cmass1X = cmass1.getX();
                    cmass2X = cmass2.getX();
                    cmass1Y = cmass1.getY();
                    cmass2Y = cmass2.getY();
                    if (currentMassX < cmass1X && currentMassX < cmass2X) {
                        //prune
                    } else if (cmass1X != cmass2X && cmass1Y != cmass2Y) {
                        slopeOfLine = (cmass1Y - cmass2Y) / (cmass1X - cmass2X);
                        currentMassOldX = currentMass.getOldX();
                        currentMassOldY = currentMass.getOldY();
                        yIntercept = cmass1Y - slopeOfLine * cmass1X;
                        resultNew = throwPointInLine(currentMassX, currentMassY, yIntercept, slopeOfLine);
                        double mass1OldX = cmass1.getOldX();
                        double mass2OldX = cmass2.getOldX();
                        double mass1OldY = cmass1.getOldY();
                        double mass2OldY = cmass2.getOldY();
                        slopeOfLine = (mass1OldY - mass2OldY) / (mass1OldX - mass2OldX);
                        yIntercept = mass1OldY - slopeOfLine * mass1OldX;
                        resultOld = throwPointInLine(currentMassOldX, currentMassOldY, yIntercept, slopeOfLine);
                        horizontalLine.setLine(currentMassX, currentMassY, 10000.0, currentMassY);
                        lineNew.setLine(cmass1X, cmass1Y, cmass2X, cmass2Y);
                        lineOld.setLine(mass1OldX, mass1OldY, mass2OldX, mass2OldY);
                        massLine.setLine(currentMassX, currentMassY, currentMassOldX, currentMassOldY);
                        int countIntersections = 0;
                        if (horizontalLine.intersectsLine(lineNew)) countIntersections++;
                        if (horizontalLine.intersectsLine(lineOld)) countIntersections++;
                        if (lineNew.intersectsLine(massLine) || lineOld.intersectsLine(massLine) || resultOld == 1 && resultNew == -1 && countIntersections == 1) {
                            currentMass.revertX();
                            currentMass.revertY();
                            Mass a = model2.getSpring(i).getMass1();
                            Mass b = model2.getSpring(i).getMass2();
                            if (!touched) {
                                touched = true;
                                firstContactPoint = currentMassX;
                            }
                            a.revertX();
                            b.revertX();
                            a.revertY();
                            b.revertY();
                            currentMassVx = currentMass.getOldVx();
                            currentMassVy = currentMass.getOldVy();
                            double aVx = a.getOldVx();
                            double bVx = b.getOldVx();
                            double aVy = a.getOldVy();
                            double bVy = b.getOldVy();
                            //lets [say] total (horizontal) kinetic energy is evenly distributed between 3 masses
                            double kineticEnergyX = Math.sqrt((aVx * aVx + bVx * bVx + currentMassVx * currentMassVx) / 3.0 * ENERGY_LEFT);
                            double kineticEnergyY = Math.sqrt((aVy * aVy + bVy * bVy + currentMassVy * currentMassVy) / 3.0 * ENERGY_LEFT);
                            currentMass.setVx(0 - kineticEnergyX);
                            a.setVx(kineticEnergyX);
                            b.setVx(kineticEnergyX);
                            if (slopeOfLine > 0) {
                                if (resultOld == 1) {
                                    currentMass.setVy(kineticEnergyY);
                                    a.setVy(0 - kineticEnergyY);
                                    b.setVy(0 - kineticEnergyY);
                                } else {
                                    currentMass.setVy(0 - kineticEnergyY);
                                    a.setVy(kineticEnergyY);
                                    b.setVy(kineticEnergyY);
                                }
                            } else {
                                if (resultOld == 1) {
                                    currentMass.setVy(0 - kineticEnergyY);
                                    a.setVy(kineticEnergyY);
                                    b.setVy(kineticEnergyY);
                                } else {
                                    currentMass.setVy(kineticEnergyY);
                                    a.setVy(0 - kineticEnergyY);
                                    b.setVy(0 - kineticEnergyY);
                                }
                            }
                        }
                    } else if (cmass1X == cmass2X) {
                        if (currentMassX < cmass1X) {
                        } else if (currentMassX < cmass1X + SPEED_LIMIT) {
                            currentMass.revertX();
                            //currentMass.revertY();
                            Mass a = model2.getSpring(i).getMass1();
                            Mass b = model2.getSpring(i).getMass2();
                            if (!touched) {
                                touched = true;
                                firstContactPoint = currentMassX;
                            }
                            a.revertX();
                            b.revertX();
                            currentMassVx = currentMass.getOldVx();
                            currentMassVy = currentMass.getOldVy();
                            double aVx = a.getOldVx();
                            double bVx = b.getOldVx();
                            //lets [say] total (horizontal) kinetic energy is evenly distributed between 3 masses
                            kineticEnergyX1 = Math.sqrt((aVx * aVx + bVx * bVx + currentMassVx * currentMassVx) / 3.0 * ENERGY_LEFT);
                            currentMass.setVx(0 - kineticEnergyX1);
                            a.setVx(kineticEnergyX1);
                            b.setVx(kineticEnergyX1);
                        }
                    } else if (cmass1Y == cmass2Y) {
                        if (currentMassY > cmass1Y) {
                            //no collision, pruned
                        } else if (currentMassY < cmass1Y + SPEED_LIMIT) {
                            //currentMass.revertX();
                            currentMass.revertY();
                            Mass a = model2.getSpring(i).getMass1();
                            Mass b = model2.getSpring(i).getMass2();
                            if (!touched) {
                                touched = true;
                                firstContactPoint = currentMassX;
                            }
                            a.revertY();
                            b.revertY();
                            currentMassVy = currentMass.getOldVy();
                            double aVy = a.getOldVy();
                            double bVy = b.getOldVy();
                            //lets [say] total (horizontal) kinetic energy is evenly distributed between 3 masses
                            kineticEnergyY1 = Math.sqrt((aVy * aVy + bVy * bVy + currentMassVy * currentMassVy) / 3.0 * ENERGY_LEFT);
                            currentMass.setVy(kineticEnergyY1);
                            a.setVy(0 - kineticEnergyY1);
                            b.setVy(0 - kineticEnergyY1);
                        }
                    }
                }

                for (int i = 1; i <= model2.getMuscleMap().size(); i++) {
                    cmass1 = model2.getMuscle(i).getMass1();
                    cmass2 = model2.getMuscle(i).getMass2();
                    cmass1X = cmass1.getX();
                    cmass2X = cmass2.getX();
                    cmass1Y = cmass1.getY();
                    cmass2Y = cmass2.getY();
                    if (currentMassX < cmass1X && currentMassX < cmass2X) {
                        //prune
                    } else if (cmass1X != cmass2X && cmass1Y != cmass2Y) {
                        slopeOfLine = (cmass1Y - cmass2Y) / (cmass1X - cmass2X);
                        currentMassOldX = currentMass.getOldX();
                        currentMassOldY = currentMass.getOldY();
                        //y=mx+c
                        yIntercept = cmass1Y - slopeOfLine * cmass1X;
                        resultNew = throwPointInLine(currentMassX, currentMassY, yIntercept, slopeOfLine);
                        double mass1OldX = cmass1.getOldX();
                        double mass2OldX = cmass2.getOldX();
                        double mass1OldY = cmass1.getOldY();
                        double mass2OldY = cmass2.getOldY();
                        slopeOfLine = (mass1OldY - mass2OldY) / (mass1OldX - mass2OldX);
                        yIntercept = mass1OldY - slopeOfLine * mass1OldX;
                        resultOld = throwPointInLine(currentMassOldX, currentMassOldY, yIntercept, slopeOfLine);
                        horizontalLine.setLine(currentMassX, currentMassY, 10000.0, currentMassY);
                        lineNew.setLine(cmass1X, cmass1Y, cmass2X, cmass2Y);
                        lineOld.setLine(mass1OldX, mass1OldY, mass2OldX, mass2OldY);
                        massLine.setLine(currentMassX, currentMassY, currentMassOldX, currentMassOldY);
                        int countIntersections = 0;
                        if (horizontalLine.intersectsLine(lineNew)) countIntersections++;
                        if (horizontalLine.intersectsLine(lineOld)) countIntersections++;
                        if (lineNew.intersectsLine(massLine) || lineOld.intersectsLine(massLine) || resultOld == 1 && resultNew == -1 && countIntersections == 1) {
                            currentMass.revertX();
                            currentMass.revertY();
                            Mass a = model2.getMuscle(i).getMass1();
                            Mass b = model2.getMuscle(i).getMass2();
                            if (!touched) {
                                touched = true;
                                firstContactPoint = currentMassX;
                            }
                            a.revertX();
                            b.revertX();
                            a.revertY();
                            b.revertY();
                            currentMassVx = currentMass.getOldVx();
                            currentMassVy = currentMass.getOldVy();
                            double aVx = a.getOldVx();
                            double bVx = b.getOldVx();
                            double aVy = a.getOldVy();
                            double bVy = b.getOldVy();
                            //lets [say] total (horizontal) kinetic energy is evenly distributed between 3 masses
                            kineticEnergyX1 = Math.sqrt((aVx * aVx + bVx * bVx + currentMassVx * currentMassVx) / 3.0 * ENERGY_LEFT);
                            kineticEnergyY1 = Math.sqrt((aVy * aVy + bVy * bVy + currentMassVy * currentMassVy) / 3.0 * ENERGY_LEFT);
                            currentMass.setVx(0 - kineticEnergyX1);
                            a.setVx(kineticEnergyX1);
                            b.setVx(kineticEnergyX1);
                            if (slopeOfLine > 0) {//old Slope!
                                if (resultOld == 1) { //on LHS
                                    currentMass.setVy(kineticEnergyY1);
                                    a.setVy(0 - kineticEnergyY1);
                                    b.setVy(0 - kineticEnergyY1);
                                } else {
                                    currentMass.setVy(0 - kineticEnergyY1);
                                    a.setVy(kineticEnergyY1);
                                    b.setVy(kineticEnergyY1);
                                }
                            } else {
                                if (resultOld == 1) { //if on LHS
                                    currentMass.setVy(0 - kineticEnergyY1);
                                    a.setVy(kineticEnergyY1);
                                    b.setVy(kineticEnergyY1);
                                } else {
                                    currentMass.setVy(kineticEnergyY1);
                                    a.setVy(0 - kineticEnergyY1);
                                    b.setVy(0 - kineticEnergyY1);
                                }
                            }
                        }
                    } else if (cmass1X == cmass2X) {
                        if (currentMassX < cmass1X) {
                        } else if (currentMassX <= cmass1X + SPEED_LIMIT) {
                            currentMass.revertX();
                            Mass a = model2.getMuscle(i).getMass1();
                            Mass b = model2.getMuscle(i).getMass2();
                            if (!touched) {
                                touched = true;
                                firstContactPoint = currentMassX;
                            }
                            a.revertX();
                            b.revertX();
                            currentMassVx = currentMass.getOldVx();
                            currentMassVy = currentMass.getOldVy();
                            double aVx = a.getOldVx();
                            double bVx = b.getOldVx();
                            //lets [say] total (horizontal) kinetic energy is evenly distributed between 3 masses
                            kineticEnergyX1 = Math.sqrt((aVx * aVx + bVx * bVx + currentMassVx * currentMassVx) / 3.0 * ENERGY_LEFT);
                            currentMass.setVx(0 - kineticEnergyX1);
                            a.setVx(kineticEnergyX1);
                            b.setVx(kineticEnergyX1);
                        }
                    } else if (cmass1Y == cmass2Y) {
                        if (currentMassY > cmass1Y) {
                            //no collision, pruned
                        } else if (currentMassY <= cmass1Y - SPEED_LIMIT) {
                            currentMass.revertY();
                            Mass a = model2.getMuscle(i).getMass1();
                            Mass b = model2.getMuscle(i).getMass2();
                            if (!touched) {
                                touched = true;
                                firstContactPoint = currentMassX;
                            }
                            a.revertY();
                            b.revertY();
                            currentMassVy = currentMass.getOldVy();
                            double aVy = a.getOldVy();
                            double bVy = b.getOldVy();
                            //lets [say] total (horizontal) kinetic energy is evenly distributed between 3 masses
                            kineticEnergyY1 = Math.sqrt((aVy * aVy + bVy * bVy + currentMassVy * currentMassVy) / 3.0 * ENERGY_LEFT);
                            currentMass.setVy(kineticEnergyY1);
                            a.setVy(0 - kineticEnergyY1);
                            b.setVy(0 - kineticEnergyY1);
                        }
                    }
                }
            }

            /*REFERENCE MASS OF MODEL2
             ****************************************************************************************************************************************/
            for (int j = 1; j <= model2.getMassMap().size(); j++) {
                currentMass = model2.getMass(j);
                currentMassX = currentMass.getX();
                currentMassY = currentMass.getY();
                for (int i = 1; i <= model1.getSpringMap().size(); i++) {//can be optimised by using 1 loop only
                    cmass1 = model1.getSpring(i).getMass1();
                    cmass2 = model1.getSpring(i).getMass2();
                    cmass1X = cmass1.getX();
                    cmass2X = cmass2.getX();
                    cmass1Y = cmass1.getY();
                    cmass2Y = cmass2.getY();
                    if (currentMassX > cmass1X && currentMassX > cmass2X) {//not collided
                        //prune
                    } else if (cmass1X != cmass2X && cmass1Y != cmass2Y) { // not vertical / horizontal
                        slopeOfLine = (cmass1Y - cmass2Y) / (cmass1X - cmass2X);
                        currentMassOldX = currentMass.getOldX();
                        currentMassOldY = currentMass.getOldY();
                        //y=mx+c
                        yIntercept = cmass1Y - slopeOfLine * cmass1X;
                        resultNew = throwPointInLine(currentMassX, currentMassY, yIntercept, slopeOfLine);
                        double mass1OldX = cmass1.getOldX();
                        double mass2OldX = cmass2.getOldX();
                        double mass1OldY = cmass1.getOldY();
                        double mass2OldY = cmass2.getOldY();
                        slopeOfLine = (mass1OldY - mass2OldY) / (mass1OldX - mass2OldX);
                        yIntercept = mass1OldY - slopeOfLine * mass1OldX;
                        resultOld = throwPointInLine(currentMassOldX, currentMassOldY, yIntercept, slopeOfLine);
                        horizontalLine.setLine(currentMassX, currentMassY, 10000.0, currentMassY);
                        lineNew.setLine(cmass1X, cmass1Y, cmass2X, cmass2Y);
                        lineOld.setLine(mass1OldX, mass1OldY, mass2OldX, mass2OldY);
                        massLine.setLine(currentMassX, currentMassY, currentMassOldX, currentMassOldY);
                        int countIntersections = 0;
                        if (horizontalLine.intersectsLine(lineNew)) countIntersections++;
                        if (horizontalLine.intersectsLine(lineOld)) countIntersections++;
                        if (lineNew.intersectsLine(massLine) || lineOld.intersectsLine(massLine) || resultOld == -1 && resultNew == 1 && countIntersections == 1) {
                            currentMass.revertX();
                            currentMass.revertY();
                            Mass a = model1.getSpring(i).getMass1();
                            Mass b = model1.getSpring(i).getMass2();
                            if (!touched) {
                                touched = true;
                                firstContactPoint = currentMassX;
                            }
                            a.revertX();
                            b.revertX();
                            a.revertY();
                            b.revertY();
                            currentMassVx = currentMass.getOldVx();
                            currentMassVy = currentMass.getOldVy();
                            double aVx = a.getOldVx();
                            double bVx = b.getOldVx();
                            double aVy = a.getOldVy();
                            double bVy = b.getOldVy();
                            //lets [say] total (horizontal) kinetic energy is evenly distributed between 3 masses
                            kineticEnergyX1 = Math.sqrt((aVx * aVx + bVx * bVx + currentMassVx * currentMassVx) / 3.0 * ENERGY_LEFT);
                            kineticEnergyY1 = Math.sqrt((aVy * aVy + bVy * bVy + currentMassVy * currentMassVy) / 3.0 * ENERGY_LEFT);
                            currentMass.setVx(kineticEnergyX1);
                            a.setVx(0 - kineticEnergyX1);
                            b.setVx(0 - kineticEnergyX1);
                            if (slopeOfLine > 0) {
                                if (resultOld == -1) {
                                    currentMass.setVy(0 - kineticEnergyY1);
                                    a.setVy(kineticEnergyY1);
                                    b.setVy(kineticEnergyY1);
                                } else {
                                    currentMass.setVy(kineticEnergyY1);
                                    a.setVy(0 - kineticEnergyY1);
                                    b.setVy(0 - kineticEnergyY1);
                                }
                            } else { //slope<0
                                if (resultOld == -1) { //if on RHS
                                    currentMass.setVy(kineticEnergyY1);
                                    a.setVy(0 - kineticEnergyY1);
                                    b.setVy(0 - kineticEnergyY1);
                                }
                                else {
                                    currentMass.setVy(0 - kineticEnergyY1);
                                    a.setVy(kineticEnergyY1);
                                    b.setVy(kineticEnergyY1);
                                }
                            }
                        }
                    } else if (cmass1X == cmass2X) { //cmass1X==cmass2X
                        if (currentMassX > cmass1X) {
                        } else if (currentMassX > cmass1X - SPEED_LIMIT) {
                            currentMass.revertX();
                            //currentMass.revertY();
                            Mass a = model1.getSpring(i).getMass1();
                            Mass b = model1.getSpring(i).getMass2();
                            if (!touched) {
                                touched = true;
                                firstContactPoint = currentMassX;
                            }
                            a.revertX();
                            b.revertX();
                            currentMassVx = currentMass.getOldVx();
                            currentMassVy = currentMass.getOldVy();
                            double aVx = a.getOldVx();
                            double bVx = b.getOldVx();
                            //lets [say] total (horizontal) kinetic energy is evenly distributed between 3 masses
                            kineticEnergyX1 = Math.sqrt((aVx * aVx + bVx * bVx + currentMassVx * currentMassVx) / 3.0 * ENERGY_LEFT);
                            currentMass.setVx(kineticEnergyX1);
                            a.setVx(0 - kineticEnergyX1);
                            b.setVx(0 - kineticEnergyX1);
                        }
                    } else if (cmass1Y == cmass2Y) {
                        if (currentMassY > cmass1Y) {
                            //no collision, pruned
                        }
                        else if (currentMassY < cmass1Y - SPEED_LIMIT) {
                            //currentMass.revertX();
                            currentMass.revertY();
                            Mass a = model1.getSpring(i).getMass1();
                            Mass b = model1.getSpring(i).getMass2();
                            if (!touched) {
                                touched = true;
                                firstContactPoint = currentMassX;
                            }
                            a.revertY();
                            b.revertY();
                            currentMassVy = currentMass.getOldVy();
                            double aVy = a.getOldVy();
                            double bVy = b.getOldVy();
                            //lets [say] total (horizontal) kinetic energy is evenly distributed between 3 masses
                            kineticEnergyY1 = Math.sqrt((aVy * aVy + bVy * bVy + currentMassVy * currentMassVy) / 3.0 * ENERGY_LEFT);
                            currentMass.setVy(kineticEnergyY1);
                            a.setVy(0 - kineticEnergyY1);
                            b.setVy(0 - kineticEnergyY1);
                        }
                    }
                }

                for (int i = 1; i <= model1.getMuscleMap().size(); i++) {//can be optimised by using 1 loop only
                    cmass1 = model1.getMuscle(i).getMass1();
                    cmass2 = model1.getMuscle(i).getMass2();
                    cmass1X = cmass1.getX();
                    cmass2X = cmass2.getX();
                    cmass1Y = cmass1.getY();
                    cmass2Y = cmass2.getY();
                    if (currentMassX > cmass1X && currentMassX > cmass2X) {
                        //prune
                    } else if (cmass1X != cmass2X && cmass1Y != cmass2Y) { // not vertical / horizontal
                        slopeOfLine = (cmass1Y - cmass2Y) / (cmass1X - cmass2X);
                        currentMassOldX = currentMass.getOldX();
                        currentMassOldY = currentMass.getOldY();
                        //y=mx+c
                        yIntercept = cmass1Y - slopeOfLine * cmass1X;
                        resultNew = throwPointInLine(currentMassX, currentMassY, yIntercept, slopeOfLine);
                        double mass1OldX = cmass1.getOldX();
                        double mass2OldX = cmass2.getOldX();
                        double mass1OldY = cmass1.getOldY();
                        double mass2OldY = cmass2.getOldY();
                        slopeOfLine = (mass1OldY - mass2OldY) / (mass1OldX - mass2OldX);
                        yIntercept = mass1OldY - slopeOfLine * mass1OldX;
                        resultOld = throwPointInLine(currentMassOldX, currentMassOldY, yIntercept, slopeOfLine);
                        horizontalLine.setLine(currentMassX, currentMassY, 10000.0, currentMassY);
                        lineNew.setLine(cmass1X, cmass1Y, cmass2X, cmass2Y);
                        lineOld.setLine(mass1OldX, mass1OldY, mass2OldX, mass2OldY);
                        massLine.setLine(currentMassX, currentMassY, currentMassOldX, currentMassOldY);
                        int countIntersections = 0;
                        if (horizontalLine.intersectsLine(lineNew)) countIntersections++;
                        if (horizontalLine.intersectsLine(lineOld)) countIntersections++;
                        if (lineNew.intersectsLine(massLine) || lineOld.intersectsLine(massLine) || resultOld == -1 && resultNew == 1 && countIntersections == 1) {
                            currentMass.revertX();
                            currentMass.revertY();
                            Mass a = model1.getMuscle(i).getMass1();
                            Mass b = model1.getMuscle(i).getMass2();
                            if (!touched) {
                                touched = true;
                                firstContactPoint = currentMassX;
                            }
                            a.revertX();
                            b.revertX();
                            a.revertY();
                            b.revertY();
                            currentMassVx = currentMass.getOldVx();
                            currentMassVy = currentMass.getOldVy();
                            double aVx = a.getOldVx();
                            double bVx = b.getOldVx();
                            double aVy = a.getOldVy();
                            double bVy = b.getOldVy();
                            //lets [say] total (horizontal) kinetic energy is evenly distributed between 3 masses
                            kineticEnergyX1 = Math.sqrt((aVx * aVx + bVx * bVx + currentMassVx * currentMassVx) / 3.0 * ENERGY_LEFT);
                            kineticEnergyY1 = Math.sqrt((aVy * aVy + bVy * bVy + currentMassVy * currentMassVy) / 3.0 * ENERGY_LEFT);
                            currentMass.setVx(kineticEnergyX1);
                            a.setVx(0 - kineticEnergyX1);
                            b.setVx(0 - kineticEnergyX1);
                            if (slopeOfLine > 0) {
                                if (resultOld == -1) {
                                    currentMass.setVy(0 - kineticEnergyY1);
                                    a.setVy(kineticEnergyY1);
                                    b.setVy(kineticEnergyY1);
                                } else {
                                    currentMass.setVy(kineticEnergyY1);
                                    a.setVy(0 - kineticEnergyY1);
                                    b.setVy(0 - kineticEnergyY1);
                                }
                            } else {
                                if (resultOld == -1) {
                                    currentMass.setVy(kineticEnergyY1);
                                    a.setVy(0 - kineticEnergyY1);
                                    b.setVy(0 - kineticEnergyY1);
                                } else {
                                    currentMass.setVy(0 - kineticEnergyY1);
                                    a.setVy(kineticEnergyY1);
                                    b.setVy(kineticEnergyY1);
                                }
                            }
                        }
                    } else if (cmass1X == cmass2X) {
                        if (currentMassX > cmass1X) {
                        } else if (currentMassX > cmass1X - SPEED_LIMIT) {
                            currentMass.revertX();
                            //currentMass.revertY();
                            Mass a = model1.getMuscle(i).getMass1();
                            Mass b = model1.getMuscle(i).getMass2();
                            if (!touched) {
                                touched = true;
                                firstContactPoint = currentMassX;
                            }
                            a.revertX();
                            b.revertX();
                            currentMassVx = currentMass.getOldVx();
                            currentMassVy = currentMass.getOldVy();
                            double aVx = a.getOldVx();
                            double bVx = b.getOldVx();
                            //lets [say] total (horizontal) kinetic energy is evenly distributed between 3 masses
                            kineticEnergyX1 = Math.sqrt((aVx * aVx + bVx * bVx + currentMassVx * currentMassVx) / 3.0 * ENERGY_LEFT);
                            currentMass.setVx(kineticEnergyX1);
                            a.setVx(0 - kineticEnergyX1);
                            b.setVx(0 - kineticEnergyX1);
                        }
                    } else if (cmass1Y == cmass2Y) {
                        if (currentMassY > cmass1Y) {
                            //no collision, pruned
                        } else if (currentMassY < cmass1Y - SPEED_LIMIT) {
                            //currentMass.revertX();
                            currentMass.revertY();
                            Mass a = model1.getMuscle(i).getMass1();
                            Mass b = model1.getMuscle(i).getMass2();
                            if (!touched) {
                                touched = true;
                                firstContactPoint = currentMassX;
                            }
                            a.revertY();
                            b.revertY();
                            currentMassVy = currentMass.getOldVy();
                            double aVy = a.getOldVy();
                            double bVy = b.getOldVy();
                            //lets [say] total (horizontal) kinetic energy is evenly distributed between 3 masses
                            kineticEnergyY1 = Math.sqrt((aVy * aVy + bVy * bVy + currentMassVy * currentMassVy) / 3.0 * ENERGY_LEFT);
                            currentMass.setVy(kineticEnergyY1);
                            a.setVy(0 - kineticEnergyY1);
                            b.setVy(0 - kineticEnergyY1);
                        }
                    }
                }
            }
        }
    }

    private int throwPointInLine(double x, double y, double yInter, double slope) {
        //y-mx-c, returns 1 if on the left
        int feedback = 1;
        double result = y - (slope * x) - yInter;
        if (slope > 0) {
            if (result < 0)
                feedback = -1;
            else if (result == 0)
                feedback = 0;
        } else if (slope < 0) {
            if (result > 0)
                feedback = -1;
            else if (result == 0)
                feedback = 0;
        }
        return feedback;
    }
}