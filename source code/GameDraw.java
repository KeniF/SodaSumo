/**
 * @(#)GameDraw.java
 *
 *
 * @author Kenneth "KeniF" Lam
 * @version 1.00 2007/10/15
 */

import java.awt.*;
import javax.swing.*;
import java.util.Date;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.awt.geom.Ellipse2D;

public class GameDraw extends JComponent{ //so it can be added like a "window"
  private static final int FRAME_DELAY=20; //ms 30ms~33.33fps
  private static double MASS_SIZE=4.0;
  private static float LINE_WIDTH=0.4f;//float
  private static double SHIFT=MASS_SIZE/2.0; //Shift needed because specified point is ellipse's top-left
  private double HEIGHT=298.0;//need to invert height as top-left is (0,0)
  private static final double GROUND_HEIGHT=0.0;
  private static final double SURFACE_FRICTION=0.1;
  private static final double SURFACE_REFLECTION=-0.75;// x velocity left when hitting ground
  private static final double MODEL_REFLECTION=-0.75;//y velocity left when hitting ground
  private static final double SPEED_LIMIT=10.0;//solves model explosion problem!!
  private static final double ENERGY_LEFT=0.95;
  private double TIME_LIMIT=15000.0;//milliseconds
  private static final RenderingHints rh=new RenderingHints(RenderingHints.KEY_ANTIALIASING,
    RenderingHints.VALUE_ANTIALIAS_ON);
  //private int fps=0;
  private Thread testThread;
  //One for each model, allows reversing
  private double noOfFrames=0.0,noOfFrames1=0.0,noOfFrames2=0.0,gameFrames=0.0;
  private Model model1,model2;//left, right
  private Graphics2D gfx2d;
  private static boolean run=false;
  private double[][]m1Acceleration,m2Acceleration;//x&y directions
  private double[][]modelValues=new double[2][6];
  private boolean invertM1=false, invertM2=false;
  private double newVelocityX, newVelocityY,newPositionX,
  newPositionY,oldVelocityY,oldVelocityX,oldPositionX,oldPositionY;//newPositions
  private boolean needToDrawMass=false,needToDrawSpring=false,
                                                            needToDrawMuscle=false;//drawing
  private double dMass1X,dMass1Y,dMass2X,dMass2Y;//for drawing
  private Muscle getMuscle;//for drawing
  private Spring getSpring;//for drawing
  private Mass drawMass1,drawMass2;//for drawing
  private double massNo,springNo,muscleNo,biggest;
  private double amp,phase,rLength,newRLength,lengthX,lengthY,length,extension,
                       resultantAcceleration,angle,mass1X,mass1Y,mass2X,mass2Y;//Accelerate springs
  private int loadMass1,loadMass2,totalNo,resultNew,resultOld,fResult;
  private Mass currentMass,cmass1,cmass2;
  private double slopeOfLine,yIntercept,reachableX;
  private double currentMassX,currentMassY,currentMassOldX,currentMassOldY,currentMassVx,currentMassVy;
  private double cmass1X=0,cmass2X=0,cmass1Y=0,cmass2Y=0,cAngle=0,width=0,midPoint,kineticEnergyX,kineticEnergyY;
  private Line2D.Double horizontalLine=new Line2D.Double();
  private Line2D.Double lineOld=new Line2D.Double();
  private Line2D.Double lineNew=new Line2D.Double();
  private Line2D.Double massLine=new Line2D.Double();
  private Ellipse2D.Double g2dEllipse=new Ellipse2D.Double();
  private Line2D.Double g2dLine=new Line2D.Double();
  private double firstContactPoint=0;
  private boolean touched=false;
  private static final Font font=new Font("Arial", Font.PLAIN,12);
  private static final Font rFont=new Font("Arial", Font.PLAIN, 20);
  private String resultMessage="";
  
  public GameDraw() {
  }//GameDraw constructor
  public void invertM1(){
    invertM1=!invertM1;
  }//invertM1
  public void invertM2(){
    invertM2=!invertM2;
  }//invertM2

  public void paint(Graphics g){
    gfx2d=(Graphics2D)g;//to use Graphics2D methods
    BasicStroke bs=new BasicStroke(LINE_WIDTH);
    gfx2d.setStroke(bs);
    gfx2d.setColor(Color.BLACK);//color of pen
    gfx2d.setRenderingHints(rh);//turns on anti-aliasing
    if(model1!=null)
      drawModel(model1);
    if(model2!=null)
      drawModel(model2);
    if(touched)
      drawVerticalLine();
    if(!resultMessage.equals(""))
      drawResult();
  }//paint

  public void pause(){
    run=false;
  }//pause
  
  public void setTimeLimit(double milliseconds){
    TIME_LIMIT=milliseconds;
  }//
  
  public void printSystemMessage(){
    
  }//printSystemMessage
  
  private void drawVerticalLine(){//line to show firstContactPoint
    gfx2d.setColor(Color.GRAY);
    g2dLine.setLine(firstContactPoint,HEIGHT+10.0,firstContactPoint,HEIGHT-1000.0);
    gfx2d.draw(g2dLine);
    gfx2d.setFont(font);
  }//drawVerticalLine
  
  private void drawResult(){
    gfx2d.setColor(Color.BLUE.darker());
    gfx2d.setFont(rFont);
    gfx2d.drawString(resultMessage, (int)Sodasumo.GAME_WIDTH/2-150, 50);
  }//
  
  private void drawModel(Model m){
    //VERY INEFFICIENT, TO BE ENHANCED
    //Date timerStart=new Date();
    massNo=m.totalNoOfMass();
    springNo=m.totalNoOfSpring();
    muscleNo=m.totalNoOfMuscle();
    biggest=0.0;
      	//find biggest amount among mass/spring/muscle
     biggest=Math.max(Math.max(massNo,springNo),muscleNo);
     for(int i=1; i<=biggest;i++){ //ONLY ONE FOR LOOP!
      	if(needToDrawMass&&massNo<i)needToDrawMass=false;
      	if(needToDrawSpring&&springNo<i)needToDrawSpring=false;
      	if(needToDrawMuscle&&muscleNo<i)needToDrawMuscle=false;
        if(needToDrawMass){
          drawMass1=m.getMass(i);
          g2dEllipse.setFrame(drawMass1.getX()-SHIFT,
          HEIGHT-(drawMass1.getY()+SHIFT),MASS_SIZE,MASS_SIZE);
          gfx2d.fill(g2dEllipse);
        }//if Mass
        if(needToDrawSpring){
        	getSpring=m.getSpring(i);
        	drawMass1=getSpring.getMass1();
        	drawMass2=getSpring.getMass2();
          g2dLine.setLine(drawMass1.getX(),HEIGHT-drawMass1.getY(),
                                  drawMass2.getX(),HEIGHT-drawMass2.getY() );                                                 
        	gfx2d.draw(g2dLine);
        }//if spring
        if(needToDrawMuscle){
        	getMuscle=m.getMuscle(i);
        	drawMass1=getMuscle.getMass1();
        	drawMass2=getMuscle.getMass2();
          dMass1X=drawMass1.getX();
        	dMass1Y=drawMass1.getY();
        	dMass2X=drawMass2.getX();
        	dMass2Y=drawMass2.getY();
          g2dLine.setLine(dMass1X,HEIGHT-dMass1Y,
                                  dMass2X,HEIGHT-dMass2Y);
          gfx2d.draw(g2dLine);
          g2dEllipse.setFrame((dMass1X+dMass2X)/2.0-1.5,
                         HEIGHT-((dMass1Y+dMass2Y)/2.0+1.5),3.0,3.0);
          gfx2d.fill(g2dEllipse);
        }//if muscle
      }//for      	
      needToDrawMuscle=true;
      needToDrawMass=true;
      needToDrawSpring=true;
      //Date timerStop=new Date();
      //System.out.println(timerStop.getTime()-timerStart.getTime());
  }//drawModel

  public void init(){
    run=true;
    noOfFrames1=0.0;
    noOfFrames2=0.0;
    gameFrames=0.0;
    m1Acceleration=new double[model1.totalNoOfMass()][2];
    m2Acceleration=new double[model2.totalNoOfMass()][2];
    touched=false;
    resultMessage="";
  }//init

  public void provideModel1(Model model){
    run=false;
    model1=null;
    model1=model;
    //Model Translation
    double[]br=model1.getBoundingRectangle();
    //br[3]=boundRight
    double shiftRight=Sodasumo.GAME_WIDTH/2.0-br[3]-10.0;//-Math.random()*10;
    for(int i=1;i<=model1.totalNoOfMass();i++){
      model1.getMass(i).setX(model1.getMass(i).getX()+shiftRight);
    }//for
    //obtain model values
    modelValues[0][0]=model1.getFriction();
    modelValues[0][1]=model1.getSpringyness();
    modelValues[0][2]=model1.getWavePhase();
    modelValues[0][3]=model1.getWaveSpeed();
    modelValues[0][4]=model1.getWaveAmplitude();
    modelValues[0][5]=model1.getGravity();
  }//drawMasses

  public void provideModel2(Model model){
    run=false;
    model2=null;
    model2=model;
    //Model Translation
    double[] br=model2.getBoundingRectangle();
    //br[2]=boundLeft
    double shiftRight=Sodasumo.GAME_WIDTH/2.0-br[2]+10.0;//+Math.random()*10;
    for(int i=1;i<=model2.totalNoOfMass();i++){
      model2.getMass(i).setX(model2.getMass(i).getX()+shiftRight);
    }//for
    //obtain model values
    modelValues[1][0]=model2.getFriction();
    modelValues[1][1]=model2.getSpringyness();
    modelValues[1][2]=model2.getWavePhase();
    modelValues[1][3]=model2.getWaveSpeed();
    modelValues[1][4]=model2.getWaveAmplitude();
    modelValues[1][5]=model2.getGravity();
  }//drawMasses

  public void startDraw(){
    testThread=new Thread(new Runnable(){
      public void run(){
        Thread curThread=Thread.currentThread();
        long beforeRun=System.currentTimeMillis();
        //long time=System.currentTimeMillis();
        while(curThread==testThread){
         if(run){
          if(gameFrames>TIME_LIMIT/FRAME_DELAY){
           gameEnds();
          }//if 
          else{
          physics();
          repaint();
          gameFrames+=1.0;
          if(!invertM1)
            noOfFrames1+=1.0;
          else noOfFrames1-=1.0;
          if(!invertM2)
            noOfFrames2+=1.0;
          else noOfFrames2-=1.0;
          }//else
         }//if run
          try{
          	//to keep a constant framerate depending on how far we are behind :D
            beforeRun+=FRAME_DELAY;
            /*fps++;
            if(System.currentTimeMillis()-time>1000){
              System.out.println(fps);
              fps=0;
              time+=1000;
             }*/
            Thread.sleep(Math.max(0,beforeRun-System.currentTimeMillis()));
          }//try
          catch(InterruptedException e){System.out.println(e);}// catch
        } //while
      }//run
    }); // Runnable
    testThread.start();
  } //startDraw
  
  //happens when 1 model gets pushed out of the ring / timeout
  private void gameEnds(){//returns integer value telling result of model1
    run=false;
    resultMessage(currentScore());
    repaint();
  }//gameEnds
  
  private String resultMessage(int score){
    if(score==0)
      resultMessage="Draw - They are equally good!";
    else if(score>0)
      resultMessage=model1.getName()+" wins! Score:"+score;
    else{
      score=0-score;
      resultMessage=model2.getName()+" wins! Score:"+score;
    }//else
    return resultMessage;
  }//resultMessage
  
  private int currentScore(){
    return (int)( (model1.getBoundRight()-firstContactPoint+
                   model2.getBoundLeft()-firstContactPoint)/20);
  }//currentScore

  private void physics(){
    if(isRunnable()){
      //create an acceleration array for all masses
      //adds up all accleration to calculate new velocity hence position
      //java automatically sets array values to 0.0
      m1Acceleration=new double[model1.totalNoOfMass()][2];
      m2Acceleration=new double[model2.totalNoOfMass()][2];
      accelerateSprings(model1,1,false);//accelerate springs
      accelerateSprings(model1,1,true);//accelerate Muscles
      newPositions(1);
      accelerateSprings(model2,2,true);
      accelerateSprings(model2,2,false);
      newPositions(2);
      doCollision();
    }//if
  }//physics
  
  private boolean isRunnable(){
    //to be extended to include run button
    boolean b=false;
    if(model1!=null&model2!=null)
      b=true;
    return b;
  }//checkStatus

  private void accelerateSprings(Model m,int input, boolean isMuscle){
     //go through all musles/springs
    if(isMuscle)
      totalNo=m.totalNoOfMuscle();
    else
      totalNo=m.totalNoOfSpring();
    for(int i=1;i<=totalNo;i++){
      if(isMuscle){
        amp=Math.abs(m.getMuscle(i).getAmplitude());
        phase=m.getMuscle(i).getPhase();
        rLength=m.getMuscle(i).getRestLength();
        //new=old*(1.0+ waveAmplitude*muscleAmplitude*sine())
        //*2 pi to convert to radians | -wavePhase to set correct restLength of Muscle
        //don't do the period already done
        if(input==1)
          noOfFrames=noOfFrames1;
        else noOfFrames=noOfFrames2;
        newRLength=rLength*(1.0+modelValues[input-1][4]*amp*
          Math.sin(
          (modelValues[input-1][3]*noOfFrames+phase-modelValues[input-1][2])*2.0*Math.PI) );
        mass1X=m.getMuscle(i).getMass1().getX();
        mass1Y=m.getMuscle(i).getMass1().getY();
        mass2X=m.getMuscle(i).getMass2().getX();
        mass2Y=m.getMuscle(i).getMass2().getY();
        loadMass1=m.getMuscle(i).getMass1().getName();
        loadMass2=m.getMuscle(i).getMass2().getName();
        //damp=false;
      }//if isMuscle
      else{// != muscle
        //rLength=m.getSpring(i).getRestLength();
        newRLength=m.getSpring(i).getRestLength();
        mass1X=m.getSpring(i).getMass1().getX();
        mass1Y=m.getSpring(i).getMass1().getY();
        mass2X=m.getSpring(i).getMass2().getX();
        mass2Y=m.getSpring(i).getMass2().getY();
        loadMass1=m.getSpring(i).getMass1().getName();
        loadMass2=m.getSpring(i).getMass2().getName();
        //damp=true;
      }//else notMuscle
      
      lengthX=Math.abs(mass1X-mass2X);//absolute value, so angle is always +
      lengthY=Math.abs(mass1Y-mass2Y);
      length=Math.sqrt(lengthX*lengthX+lengthY*lengthY); //Pythagoras'
      extension=(length-newRLength);
      //Frictional force affects velocity only!!
      resultantAcceleration=Math.abs(modelValues[input-1][1]*extension);//F=kx=ma where m=1.0
      //gets the masses connected to the current muscle
      //Mass#109 --> 109
      angle=Math.atan(lengthY/lengthX);//in radians
      if(extension!=0.0){//if springs are pulled/pushed
        //if extension>0.0, spring pulled, resultantAcceleration is correct
        //if extension<0.0, spring pushed, resulrantAcceleration needs to be inverted
        if(extension<0.0)
          resultantAcceleration*=-1;
        if(mass1X>mass2X){
          if(mass2Y>mass1Y){
            //minus 1 because mass index starts from one but array from 0
            if(input==1){//model1
              m1Acceleration[loadMass1-1][0]-=resultantAcceleration*Math.cos(angle);
              m1Acceleration[loadMass1-1][1]+=resultantAcceleration*Math.sin(angle);
              m1Acceleration[loadMass2-1][0]+=resultantAcceleration*Math.cos(angle);
              m1Acceleration[loadMass2-1][1]-=resultantAcceleration*Math.sin(angle);
            }//if model1
            else{//model2
              m2Acceleration[loadMass1-1][0]-=resultantAcceleration*Math.cos(angle);
              m2Acceleration[loadMass1-1][1]+=resultantAcceleration*Math.sin(angle);
              m2Acceleration[loadMass2-1][0]+=resultantAcceleration*Math.cos(angle);
              m2Acceleration[loadMass2-1][1]-=resultantAcceleration*Math.sin(angle);
            }//else
          }//if mass2Y>mass1Y
          else if(mass2Y<mass1Y){
            if(input==1){//model1
              m1Acceleration[loadMass1-1][0]-=resultantAcceleration*Math.cos(angle);
              m1Acceleration[loadMass1-1][1]-=resultantAcceleration*Math.sin(angle);
              m1Acceleration[loadMass2-1][0]+=resultantAcceleration*Math.cos(angle);
              m1Acceleration[loadMass2-1][1]+=resultantAcceleration*Math.sin(angle);
            }//if model1
            else{//model2
              m2Acceleration[loadMass1-1][0]-=resultantAcceleration*Math.cos(angle);
              m2Acceleration[loadMass1-1][1]-=resultantAcceleration*Math.sin(angle);
              m2Acceleration[loadMass2-1][0]+=resultantAcceleration*Math.cos(angle);
              m2Acceleration[loadMass2-1][1]+=resultantAcceleration*Math.sin(angle);
            }//else
          }//else if
          else{//mass2Y=mass1Y m2<------------->m1
            if(input==1){//model1
              m1Acceleration[loadMass1-1][0]-=resultantAcceleration;
              m1Acceleration[loadMass2-1][0]+=resultantAcceleration;
            }//if model1
            else{//model2
              m2Acceleration[loadMass1-1][0]-=resultantAcceleration;
              m2Acceleration[loadMass2-1][0]+=resultantAcceleration;
            }//else model2
          }//else
        }//if mass1X>mass2X
        else if(mass1X<mass2X){
          if(mass2Y>mass1Y){
            //minus 1 because mass index starts from one but array from 0
            if(input==1){//model1
              m1Acceleration[loadMass1-1][0]+=resultantAcceleration*Math.cos(angle);
              m1Acceleration[loadMass1-1][1]+=resultantAcceleration*Math.sin(angle);
              m1Acceleration[loadMass2-1][0]-=resultantAcceleration*Math.cos(angle);
              m1Acceleration[loadMass2-1][1]-=resultantAcceleration*Math.sin(angle);
            }//if model1
            else{//model2
              m2Acceleration[loadMass1-1][0]+=resultantAcceleration*Math.cos(angle);
              m2Acceleration[loadMass1-1][1]+=resultantAcceleration*Math.sin(angle);
              m2Acceleration[loadMass2-1][0]-=resultantAcceleration*Math.cos(angle);
              m2Acceleration[loadMass2-1][1]-=resultantAcceleration*Math.sin(angle);
            }//else
          }//if mass2Y>mass1Y
          else if(mass2Y<mass1Y){
            if(input==1){//model1
              m1Acceleration[loadMass1-1][0]+=resultantAcceleration*Math.cos(angle);
              m1Acceleration[loadMass1-1][1]-=resultantAcceleration*Math.sin(angle);
              m1Acceleration[loadMass2-1][0]-=resultantAcceleration*Math.cos(angle);
              m1Acceleration[loadMass2-1][1]+=resultantAcceleration*Math.sin(angle);
            }//if model1
            else{//model2
              m2Acceleration[loadMass1-1][0]+=resultantAcceleration*Math.cos(angle);
              m2Acceleration[loadMass1-1][1]-=resultantAcceleration*Math.sin(angle);
              m2Acceleration[loadMass2-1][0]-=resultantAcceleration*Math.cos(angle);
              m2Acceleration[loadMass2-1][1]+=resultantAcceleration*Math.sin(angle);
            }//else
          }//else if
          else{//mass2Y=mass1Y m1<------------->m2
            if(input==1){//model1
              m1Acceleration[loadMass1-1][0]+=resultantAcceleration;//x
              m1Acceleration[loadMass2-1][0]-=resultantAcceleration;//x
            }//if model1
            else{//model2
              m2Acceleration[loadMass1-1][0]+=resultantAcceleration;//x
              m2Acceleration[loadMass2-1][0]-=resultantAcceleration;//x
            }//else model2
          }//else m2y=m1y
        }//else if
        else{//mass1X=mass2X
          if(mass1Y>mass2Y){
            if(input==1){
              m1Acceleration[loadMass1-1][1]-=resultantAcceleration;//y
              m1Acceleration[loadMass2-1][1]+=resultantAcceleration;
            }//model1
            else{
              m2Acceleration[loadMass1-1][1]-=resultantAcceleration;//y
              m2Acceleration[loadMass2-1][1]+=resultantAcceleration;
            }//model1
          }//mass1Y>mass2Y
          else if(mass1Y<mass2Y){ //mass1Y<mass2Y
            if(input==1){
              m1Acceleration[loadMass1-1][1]+=resultantAcceleration;
              m1Acceleration[loadMass2-1][1]-=resultantAcceleration;
            }//model1
            else{
              m2Acceleration[loadMass1-1][1]+=resultantAcceleration;
              m2Acceleration[loadMass2-1][1]-=resultantAcceleration;
            }//else
          }//else if
          else{//mass1Y=mass2Y?!
            System.out.println("Error: Spring without extension!");
          }//else
        }//else m1x=m2x
      }// if extension!=0
    }//for
  }//accelerateSprings

  private void newPositions(int model){
    if(model==1){
      model1.resetBoundRect();
      for(int i=1;i<=model1.totalNoOfMass();i++){
        //dampling for F=-fv
        model1.getMass(i).finallyRevert();
        oldVelocityX=model1.getMass(i).getVx();
        oldVelocityY=model1.getMass(i).getVy();
        newVelocityX=oldVelocityX+m1Acceleration[i-1][0];
        newVelocityX=newVelocityX-newVelocityX*modelValues[0][0];
        newVelocityY=oldVelocityY+m1Acceleration[i-1][1];//-modelValues[0][5];
        newVelocityY=newVelocityY-newVelocityY*modelValues[0][0];
        newVelocityY-=modelValues[0][5];//gravity(not damped!)
        if(Math.abs(newVelocityY)>SPEED_LIMIT){
        	if(newVelocityY>0)
        	  newVelocityY=SPEED_LIMIT;
        	else newVelocityY=0-SPEED_LIMIT;
        }//if
        if(Math.abs(newVelocityX)>SPEED_LIMIT){
        	if(newVelocityX>0)
        	  newVelocityX=SPEED_LIMIT;
        	else newVelocityX=0-SPEED_LIMIT;
        }//if
        
        oldPositionX=model1.getMass(i).getX();
        oldPositionY=model1.getMass(i).getY();
        newPositionX=oldPositionX+newVelocityX;
        newPositionY=oldPositionY+newVelocityY;

        //if goes through ground
        if(newPositionY<=GROUND_HEIGHT){
          if(newVelocityY<0)
            newVelocityY=newVelocityY*SURFACE_REFLECTION;
          newPositionY=GROUND_HEIGHT;
          newVelocityX*=SURFACE_FRICTION;
       }//if
        Mass model1Mass=model1.getMass(i);
        model1Mass.setVx(newVelocityX);
        model1Mass.setVy(newVelocityY);
        model1Mass.setX(newPositionX);
        model1Mass.setY(newPositionY);
        //double oldBoundRight=model1.getBoundRight();
        model1.adjustBoundRect(model1Mass);
      }//for
    }//model1
    if(model==2){
      model2.resetBoundRect();
      for(int i=1;i<=model2.totalNoOfMass();i++){
        //dampling for F=-fv
        model2.getMass(i).finallyRevert();
        oldVelocityX=model2.getMass(i).getVx();
        oldVelocityY=model2.getMass(i).getVy();
        newVelocityX=oldVelocityX+m2Acceleration[i-1][0];
        newVelocityX=newVelocityX-newVelocityX*modelValues[1][0];
        newVelocityY=oldVelocityY+m2Acceleration[i-1][1];//-modelValues[1][5];
        newVelocityY=newVelocityY-newVelocityY*modelValues[1][0];
        newVelocityY-=modelValues[1][5];//gravity(not damped!)
        if(Math.abs(newVelocityY)>SPEED_LIMIT){
        	if(newVelocityY>0)
        	  newVelocityY=SPEED_LIMIT;
        	else newVelocityY=0-SPEED_LIMIT;
        }//if
        if(Math.abs(newVelocityX)>SPEED_LIMIT){
        	if(newVelocityX>0)
        	  newVelocityX=SPEED_LIMIT;
        	else newVelocityX=0-SPEED_LIMIT;
        }//if
        oldPositionX=model2.getMass(i).getX();
        oldPositionY=model2.getMass(i).getY();
        newPositionX=oldPositionX+newVelocityX;
        newPositionY=oldPositionY+newVelocityY;

        //if goes through ground
        if(newPositionY<=GROUND_HEIGHT){
          if(newVelocityY<0)
            newVelocityY=newVelocityY*SURFACE_REFLECTION;
          newPositionY=GROUND_HEIGHT;
          newVelocityX*=SURFACE_FRICTION;
         }//if
        Mass model2Mass=model2.getMass(i);
        model2Mass.setVx(newVelocityX);
        model2Mass.setVy(newVelocityY);
        model2Mass.setX(newPositionX);
        model2Mass.setY(newPositionY);
        //double oldBoundLeft=model2.getBoundLeft();
        model2.adjustBoundRect(model2Mass);
      }//for
    }//model2
  }//newPositions
  
  private void doCollision(){
  	if(model1.getBoundRight()<model2.getBoundLeft()){
  	 //Broad-phase detection: No collision, do nothing
  	 return;
  	}//if 
  	else{//may have collided, narrow-phase detection needed
  	  //go through springs and muscles of model2
  	  for(int j=1;j<=model1.totalNoOfMass();j++){
  	  	midPoint=(model1.getBoundRight()+model1.getBoundLeft())/2.0;
  	  	currentMass=model1.getMass(j);
  	  	currentMassX=currentMass.getX();
  	  	currentMassY=currentMass.getY();
	      //Scan through Springs!!
	  	  for(int i=1;i<=model2.totalNoOfSpring();i++){//can be optimised by using 1 loop only
	  	    cmass1=model2.getSpring(i).getMass1();
	  	    cmass2=model2.getSpring(i).getMass2();
	  	    cmass1X=cmass1.getX();
	  	    cmass2X=cmass2.getX();
  	      cmass1Y=cmass1.getY();
	        cmass2Y=cmass2.getY();
          if(currentMassX<cmass1X&&currentMassX<cmass2X){//not collided
	          //prune
          }//not collided
	        else if(cmass1X!=cmass2X&&cmass1Y!=cmass2Y){ // not vertical / horizontal
  	          slopeOfLine=(cmass1Y-cmass2Y)/(cmass1X-cmass2X);
  	          currentMassOldX=currentMass.getOldX();
  	          currentMassOldY=currentMass.getOldY();
              //y=mx+c
  	          yIntercept=cmass1Y-slopeOfLine*cmass1X;
  	          resultNew=throwPointInLine(currentMassX,currentMassY,yIntercept,slopeOfLine);
  	          double mass1OldX=cmass1.getOldX();
  	          double mass2OldX=cmass2.getOldX();
  	          double mass1OldY=cmass1.getOldY();
  	          double mass2OldY=cmass2.getOldY();
  	          slopeOfLine=(mass1OldY-mass2OldY)/(mass1OldX-mass2OldX);
  	          yIntercept=mass1OldY-slopeOfLine*mass1OldX;
  	          resultOld=throwPointInLine(currentMassOldX,currentMassOldY,yIntercept,slopeOfLine);
  	          fResult=resultOld*resultNew;
              horizontalLine.setLine(currentMassX,currentMassY,10000.0,currentMassY);
              lineNew.setLine(cmass1X,cmass1Y,cmass2X,cmass2Y);
              lineOld.setLine(mass1OldX,mass1OldY,mass2OldX,mass2OldY);
              massLine.setLine(currentMassX,currentMassY,currentMassOldX,currentMassOldY);
              int countIntersections=0;
              if(horizontalLine.intersectsLine(lineNew)) countIntersections++;
              if(horizontalLine.intersectsLine(lineOld)) countIntersections++;
              if(lineNew.intersectsLine(massLine)||lineOld.intersectsLine(massLine)||resultOld==1&&resultNew==-1&&countIntersections==1){
  	  	          	currentMass.revertX();
  	  	          	currentMass.revertY();
  	  	          	Mass a=model2.getSpring(i).getMass1();
  		  	      		Mass b=model2.getSpring(i).getMass2();
                    if(!touched){
                      touched=true;
                      firstContactPoint=currentMassX;
                    }
  		  	     		  a.revertX(); b.revertX();
  		  	     		  a.revertY(); b.revertY();
  	  	          	currentMassVx=currentMass.getOldVx();
  	  	          	currentMassVy=currentMass.getOldVy();
    	  	          double aVx=a.getOldVx();
  		  	     		  double bVx=b.getOldVx();
  		  	     		  double aVy=a.getOldVy();
  		  	     		  double bVy=b.getOldVy();
                    //lets [say] total (horizontal) kinetic energy is evenly distributed between 3 masses
  		  	     		  double kineticEnergyX=Math.sqrt((aVx*aVx+bVx*bVx+currentMassVx*currentMassVx)/3.0*ENERGY_LEFT);
  		  	     		  double kineticEnergyY=Math.sqrt((aVy*aVy+bVy*bVy+currentMassVy*currentMassVy)/3.0*ENERGY_LEFT);
  		  	     		  currentMass.setVx(0-kineticEnergyX);
  		  	     		  a.setVx(kineticEnergyX); b.setVx(kineticEnergyX);
  		  	     		  if(slopeOfLine>0){//old Slope!
  		  	     		    if(resultOld==1){ //on LHS
  		  	     		      currentMass.setVy(kineticEnergyY);
  		  	     		      a.setVy(0-kineticEnergyY); b.setVy(0-kineticEnergyY);
  		  	     		    }//resultNew
  		  	     		    else{
                        currentMass.setVy(0-kineticEnergyY);
                        a.setVy(kineticEnergyY); b.setVy(kineticEnergyY);
  		  	     		    }//else resultNew!=1
  		  	     		  }//if slope>0
  		  	     		  else{ //slope<0
                      if(resultOld==1){ //if on LHS
                       currentMass.setVy(0-kineticEnergyY);
                       a.setVy(kineticEnergyY); b.setVy(kineticEnergyY);
  		  	     		  	}//if on LHS
  		  	     		  	else{
                        currentMass.setVy(kineticEnergyY);
                        a.setVy(0-kineticEnergyY); b.setVy(0-kineticEnergyY);
  		  	     		  	}//else on RHS
  		  	     		  }//slope <0
    	          	}// if
    	          	else{}// do nothing! :D
        	     //}// if collided?
  	       	}//else if cmass1X!=cmass2X
  	       	else if(cmass1X==cmass2X){ //cmass1X==cmass2X
	  	     		if(currentMassX<cmass1X){
	  	     		}//no collision, pruned
	  	     		else if(currentMassX<cmass1X+SPEED_LIMIT){
                currentMass.revertX();
                //currentMass.revertY();
                Mass a=model2.getSpring(i).getMass1();
                Mass b=model2.getSpring(i).getMass2();
                if(!touched){
                  touched=true;
                  firstContactPoint=currentMassX;
	  	     		  }
                a.revertX(); b.revertX();
                currentMassVx=currentMass.getOldVx();
                currentMassVy=currentMass.getOldVy();
                double aVx=a.getOldVx();
                double bVx=b.getOldVx();
                //lets [say] total (horizontal) kinetic energy is evenly distributed between 3 masses
                kineticEnergyX=Math.sqrt((aVx*aVx+bVx*bVx+currentMassVx*currentMassVx)/3.0*ENERGY_LEFT);
                currentMass.setVx(0-kineticEnergyX);
                a.setVx(kineticEnergyX); b.setVx(kineticEnergyX);
	  	     		}// collided with vertical line
  	       	}// else cmass1X==cmass2X
  	       	else if(cmass1Y==cmass2Y){
  	       	 if(currentMassY>cmass1Y){
  	       	   //no collision, pruned
  	       	 }//
  	       	 else if(currentMassY<cmass1Y+SPEED_LIMIT){
               //currentMass.revertX();
               currentMass.revertY();
               Mass a=model2.getSpring(i).getMass1();
               Mass b=model2.getSpring(i).getMass2();
               if(!touched){
                 touched=true;
                 firstContactPoint=currentMassX;
               }
               a.revertY(); b.revertY();
               currentMassVy=currentMass.getOldVy();
               double aVy=a.getOldVy();
               double bVy=b.getOldVy();
               //lets [say] total (horizontal) kinetic energy is evenly distributed between 3 masses
               kineticEnergyY=Math.sqrt((aVy*aVy+bVy*bVy+currentMassVy*currentMassVy)/3.0*ENERGY_LEFT);
               currentMass.setVy(kineticEnergyY);
               a.setVy(0-kineticEnergyY); b.setVy(0-kineticEnergyY);
  	       	 }//else collided with horizontal line
  	       }//else cmass1Y==cmass2Y
    	  }//for springs
    	  
    	  /*for muscles*/
        for(int i=1;i<=model2.totalNoOfMuscle();i++){//can be optimised by using 1 loop only
          cmass1=model2.getMuscle(i).getMass1();
          cmass2=model2.getMuscle(i).getMass2();
          cmass1X=cmass1.getX();
          cmass2X=cmass2.getX();
          cmass1Y=cmass1.getY();
          cmass2Y=cmass2.getY();
          if(currentMassX<cmass1X&&currentMassX<cmass2X){//not collided
            //prune
          }//not collided
          else if(cmass1X!=cmass2X&&cmass1Y!=cmass2Y){ // not vertical / horizontal
              slopeOfLine=(cmass1Y-cmass2Y)/(cmass1X-cmass2X);
              currentMassOldX=currentMass.getOldX();
              currentMassOldY=currentMass.getOldY();
              //y=mx+c
              yIntercept=cmass1Y-slopeOfLine*cmass1X;
              resultNew=throwPointInLine(currentMassX,currentMassY,yIntercept,slopeOfLine);
              double mass1OldX=cmass1.getOldX();
              double mass2OldX=cmass2.getOldX();
              double mass1OldY=cmass1.getOldY();
              double mass2OldY=cmass2.getOldY();
              slopeOfLine=(mass1OldY-mass2OldY)/(mass1OldX-mass2OldX);
              yIntercept=mass1OldY-slopeOfLine*mass1OldX;
              resultOld=throwPointInLine(currentMassOldX,currentMassOldY,yIntercept,slopeOfLine);
              fResult=resultOld*resultNew;
              horizontalLine.setLine(currentMassX,currentMassY,10000.0,currentMassY);
              lineNew.setLine(cmass1X,cmass1Y,cmass2X,cmass2Y);
              lineOld.setLine(mass1OldX,mass1OldY,mass2OldX,mass2OldY);
              massLine.setLine(currentMassX,currentMassY,currentMassOldX,currentMassOldY);
              int countIntersections=0;
              if(horizontalLine.intersectsLine(lineNew)) countIntersections++;
              if(horizontalLine.intersectsLine(lineOld)) countIntersections++;
              if(lineNew.intersectsLine(massLine)||lineOld.intersectsLine(massLine)||resultOld==1&&resultNew==-1&&countIntersections==1){
                 currentMass.revertX();
                 currentMass.revertY();
                 Mass a=model2.getMuscle(i).getMass1();
                 Mass b=model2.getMuscle(i).getMass2();
                 if(!touched){
                   touched=true;
                   firstContactPoint=currentMassX;
                 }
                 a.revertX(); b.revertX();
                 a.revertY(); b.revertY();
                 currentMassVx=currentMass.getOldVx();
                 currentMassVy=currentMass.getOldVy();
                 double aVx=a.getOldVx();
                 double bVx=b.getOldVx();
                 double aVy=a.getOldVy();
                 double bVy=b.getOldVy();
                 //lets [say] total (horizontal) kinetic energy is evenly distributed between 3 masses
                 kineticEnergyX=Math.sqrt((aVx*aVx+bVx*bVx+currentMassVx*currentMassVx)/3.0*ENERGY_LEFT);
                 kineticEnergyY=Math.sqrt((aVy*aVy+bVy*bVy+currentMassVy*currentMassVy)/3.0*ENERGY_LEFT);
                 currentMass.setVx(0-kineticEnergyX);
                 a.setVx(kineticEnergyX); b.setVx(kineticEnergyX);
                 if(slopeOfLine>0){//old Slope!
                      if(resultOld==1){ //on LHS
                        currentMass.setVy(kineticEnergyY);
                        a.setVy(0-kineticEnergyY); b.setVy(0-kineticEnergyY);
                      }//resultNew
                      else{
                        currentMass.setVy(0-kineticEnergyY);
                        a.setVy(kineticEnergyY); b.setVy(kineticEnergyY);
                      }//else resultNew!=1
                    }//if slope>0
                    else{ //slope<0
                      if(resultOld==1){ //if on LHS
                       currentMass.setVy(0-kineticEnergyY);
                       a.setVy(kineticEnergyY); b.setVy(kineticEnergyY);
                      }//if on LHS
                      else{
                        currentMass.setVy(kineticEnergyY);
                        a.setVy(0-kineticEnergyY); b.setVy(0-kineticEnergyY);
                      }//else on RHS
                    }//slope <0
               }// if
               else{}// PRUNING
             }//else if cmass1X!=cmass2X
            else if(cmass1X==cmass2X){ //cmass1X==cmass2X
              if(currentMassX<cmass1X){
              }//no collision, pruned
              else if(currentMassX<=cmass1X+SPEED_LIMIT){
                currentMass.revertX();
                //currentMass.revertY();
                Mass a=model2.getMuscle(i).getMass1();
                Mass b=model2.getMuscle(i).getMass2();
                if(!touched){
                 touched=true;
                 firstContactPoint=currentMassX;
               }
                a.revertX(); b.revertX();
                //a.revertY(); b.revertY();
                currentMassVx=currentMass.getOldVx();
                currentMassVy=currentMass.getOldVy();
                double aVx=a.getOldVx();
                double bVx=b.getOldVx();
                //lets [say] total (horizontal) kinetic energy is evenly distributed between 3 masses
                kineticEnergyX=Math.sqrt((aVx*aVx+bVx*bVx+currentMassVx*currentMassVx)/3.0*ENERGY_LEFT);
                //System.out.println(kineticEnergyX);
                currentMass.setVx(0-kineticEnergyX);
                a.setVx(kineticEnergyX); b.setVx(kineticEnergyX);
              }// collided with vertical line
            }// else cmass1X==cmass2X
            else if(cmass1Y==cmass2Y){
             if(currentMassY>cmass1Y){
               //no collision, pruned
             }//
             else if(currentMassY<=cmass1Y-SPEED_LIMIT){
               //currentMass.revertX();
               currentMass.revertY();
               Mass a=model2.getMuscle(i).getMass1();
               Mass b=model2.getMuscle(i).getMass2();
               if(!touched){
                 touched=true;
                 firstContactPoint=currentMassX;
               }
               a.revertY(); b.revertY();
               currentMassVy=currentMass.getOldVy();
               double aVy=a.getOldVy();
               double bVy=b.getOldVy();
               //lets [say] total (horizontal) kinetic energy is evenly distributed between 3 masses
               kineticEnergyY=Math.sqrt((aVy*aVy+bVy*bVy+currentMassVy*currentMassVy)/3.0*ENERGY_LEFT);
               currentMass.setVy(kineticEnergyY);
               a.setVy(0-kineticEnergyY); b.setVy(0-kineticEnergyY);
             }//else collided with horizontal line
           }//else cmass1Y==cmass2Y
        }//for MUCLES of model2
      }//for go through all mass
     
      /*REFERENCE MASS OF MODEL2
       ****************************************************************************************************************************************/
      for(int j=1;j<=model2.totalNoOfMass();j++){
        //midPoint=(model1.getBoundRight()+model1.getBoundLeft())/2.0;
        currentMass=model2.getMass(j);
        currentMassX=currentMass.getX();
        currentMassY=currentMass.getY();
        //Scan through Springs!!
        for(int i=1;i<=model1.totalNoOfSpring();i++){//can be optimised by using 1 loop only
          cmass1=model1.getSpring(i).getMass1();
          cmass2=model1.getSpring(i).getMass2();
          cmass1X=cmass1.getX();
          cmass2X=cmass2.getX();
          cmass1Y=cmass1.getY();
          cmass2Y=cmass2.getY();
          if(currentMassX>cmass1X&&currentMassX>cmass2X){//not collided
            //prune
          }//not collided
          else if(cmass1X!=cmass2X&&cmass1Y!=cmass2Y){ // not vertical / horizontal
              slopeOfLine=(cmass1Y-cmass2Y)/(cmass1X-cmass2X);
              currentMassOldX=currentMass.getOldX();
              currentMassOldY=currentMass.getOldY();
              //y=mx+c
              yIntercept=cmass1Y-slopeOfLine*cmass1X;
              resultNew=throwPointInLine(currentMassX,currentMassY,yIntercept,slopeOfLine);
              double mass1OldX=cmass1.getOldX();
              double mass2OldX=cmass2.getOldX();
              double mass1OldY=cmass1.getOldY();
              double mass2OldY=cmass2.getOldY();
              slopeOfLine=(mass1OldY-mass2OldY)/(mass1OldX-mass2OldX);
              yIntercept=mass1OldY-slopeOfLine*mass1OldX;
              resultOld=throwPointInLine(currentMassOldX,currentMassOldY,yIntercept,slopeOfLine);
              fResult=resultOld*resultNew;
              horizontalLine.setLine(currentMassX,currentMassY,10000.0,currentMassY);
              lineNew.setLine(cmass1X,cmass1Y,cmass2X,cmass2Y);
              lineOld.setLine(mass1OldX,mass1OldY,mass2OldX,mass2OldY);
              massLine.setLine(currentMassX,currentMassY,currentMassOldX,currentMassOldY);
              int countIntersections=0;
              if(horizontalLine.intersectsLine(lineNew)) countIntersections++;
              if(horizontalLine.intersectsLine(lineOld)) countIntersections++;
              if(lineNew.intersectsLine(massLine)||lineOld.intersectsLine(massLine)||resultOld==-1&&resultNew==1&&countIntersections==1){
                    currentMass.revertX();
                    currentMass.revertY();
                    Mass a=model1.getSpring(i).getMass1();
                    Mass b=model1.getSpring(i).getMass2();
                    if(!touched){
                      touched=true;
                      firstContactPoint=currentMassX;
                   }
                    a.revertX(); b.revertX();
                    a.revertY(); b.revertY();
                    currentMassVx=currentMass.getOldVx();
                    currentMassVy=currentMass.getOldVy();
                    double aVx=a.getOldVx();
                    double bVx=b.getOldVx();
                    double aVy=a.getOldVy();
                    double bVy=b.getOldVy();
                    //lets [say] total (horizontal) kinetic energy is evenly distributed between 3 masses
                    kineticEnergyX=Math.sqrt((aVx*aVx+bVx*bVx+currentMassVx*currentMassVx)/3.0*ENERGY_LEFT);
                    kineticEnergyY=Math.sqrt((aVy*aVy+bVy*bVy+currentMassVy*currentMassVy)/3.0*ENERGY_LEFT);
                    currentMass.setVx(kineticEnergyX);
                    a.setVx(0-kineticEnergyX); b.setVx(0-kineticEnergyX);
                    if(slopeOfLine>0){//old Slope!
                      if(resultOld==-1){ //on RHS
                        currentMass.setVy(0-kineticEnergyY);
                        a.setVy(kineticEnergyY); b.setVy(kineticEnergyY);
                      }//resultNew
                      else{
                        currentMass.setVy(kineticEnergyY);
                        a.setVy(0-kineticEnergyY); b.setVy(0-kineticEnergyY);
                      }//else resultNew!=1
                    }//if slope>0
                    else{ //slope<0
                      if(resultOld==-1){ //if on RHS
                       currentMass.setVy(kineticEnergyY);
                       a.setVy(0-kineticEnergyY); b.setVy(0-kineticEnergyY);
                      }//if on LHS
                      else{
                        currentMass.setVy(0-kineticEnergyY);
                        a.setVy(kineticEnergyY); b.setVy(kineticEnergyY);
                      }//else on RHS
                    }//slope <0
                  }// if
                  else{}// do nothing! :D
               //}// if collided?
            }//else if cmass1X!=cmass2X
            else if(cmass1X==cmass2X){ //cmass1X==cmass2X
              if(currentMassX>cmass1X){
              }//no collision, pruned
              else if(currentMassX>cmass1X-SPEED_LIMIT){
                currentMass.revertX();
                //currentMass.revertY();
                Mass a=model1.getSpring(i).getMass1();
                Mass b=model1.getSpring(i).getMass2();
                if(!touched){
                 touched=true;
                 firstContactPoint=currentMassX;
               }
                a.revertX(); b.revertX();
                currentMassVx=currentMass.getOldVx();
                currentMassVy=currentMass.getOldVy();
                double aVx=a.getOldVx();
                double bVx=b.getOldVx();
                //lets [say] total (horizontal) kinetic energy is evenly distributed between 3 masses
                kineticEnergyX=Math.sqrt((aVx*aVx+bVx*bVx+currentMassVx*currentMassVx)/3.0*ENERGY_LEFT);
                currentMass.setVx(kineticEnergyX);
                a.setVx(0-kineticEnergyX); b.setVx(0-kineticEnergyX);
              }// collided with vertical line
            }// else cmass1X==cmass2X
            else if(cmass1Y==cmass2Y){
             if(currentMassY>cmass1Y){
               //no collision, pruned
             }//
             else if(currentMassY<cmass1Y-SPEED_LIMIT){
               //currentMass.revertX();
               currentMass.revertY();
               Mass a=model1.getSpring(i).getMass1();
               Mass b=model1.getSpring(i).getMass2();
               if(!touched){
                 touched=true;
                 firstContactPoint=currentMassX;
               }
               a.revertY(); b.revertY();
               currentMassVy=currentMass.getOldVy();
               double aVy=a.getOldVy();
               double bVy=b.getOldVy();
               //lets [say] total (horizontal) kinetic energy is evenly distributed between 3 masses
               kineticEnergyY=Math.sqrt((aVy*aVy+bVy*bVy+currentMassVy*currentMassVy)/3.0*ENERGY_LEFT);
               currentMass.setVy(kineticEnergyY);
               a.setVy(0-kineticEnergyY); b.setVy(0-kineticEnergyY);
             }//else collided with horizontal line
           }//else cmass1Y==cmass2Y
        }//for springs
        
        /*for muscles*/
        for(int i=1;i<=model1.totalNoOfMuscle();i++){//can be optimised by using 1 loop only
          cmass1=model1.getMuscle(i).getMass1();
          cmass2=model1.getMuscle(i).getMass2();
          cmass1X=cmass1.getX();
          cmass2X=cmass2.getX();
          cmass1Y=cmass1.getY();
          cmass2Y=cmass2.getY();
          if(currentMassX>cmass1X&&currentMassX>cmass2X){//not collided
            //prune
          }//not collided
          else if(cmass1X!=cmass2X&&cmass1Y!=cmass2Y){ // not vertical / horizontal
              slopeOfLine=(cmass1Y-cmass2Y)/(cmass1X-cmass2X);
              currentMassOldX=currentMass.getOldX();
              currentMassOldY=currentMass.getOldY();
              //y=mx+c
              yIntercept=cmass1Y-slopeOfLine*cmass1X;
              resultNew=throwPointInLine(currentMassX,currentMassY,yIntercept,slopeOfLine);
              double mass1OldX=cmass1.getOldX();
              double mass2OldX=cmass2.getOldX();
              double mass1OldY=cmass1.getOldY();
              double mass2OldY=cmass2.getOldY();
              slopeOfLine=(mass1OldY-mass2OldY)/(mass1OldX-mass2OldX);
              yIntercept=mass1OldY-slopeOfLine*mass1OldX;
              resultOld=throwPointInLine(currentMassOldX,currentMassOldY,yIntercept,slopeOfLine);
              fResult=resultOld*resultNew;
              horizontalLine.setLine(currentMassX,currentMassY,10000.0,currentMassY);
              lineNew.setLine(cmass1X,cmass1Y,cmass2X,cmass2Y);
              lineOld.setLine(mass1OldX,mass1OldY,mass2OldX,mass2OldY);
              massLine.setLine(currentMassX,currentMassY,currentMassOldX,currentMassOldY);
              int countIntersections=0;
              if(horizontalLine.intersectsLine(lineNew)) countIntersections++;
              if(horizontalLine.intersectsLine(lineOld)) countIntersections++;
              if(lineNew.intersectsLine(massLine)||lineOld.intersectsLine(massLine)||resultOld==-1&&resultNew==1&&countIntersections==1){
                    currentMass.revertX();
                    currentMass.revertY();
                    Mass a=model1.getMuscle(i).getMass1();
                    Mass b=model1.getMuscle(i).getMass2();
                    if(!touched){
                      touched=true;
                      firstContactPoint=currentMassX;
                    }
                    a.revertX(); b.revertX();
                    a.revertY(); b.revertY();
                    currentMassVx=currentMass.getOldVx();
                    currentMassVy=currentMass.getOldVy();
                    double aVx=a.getOldVx();
                    double bVx=b.getOldVx();
                    double aVy=a.getOldVy();
                    double bVy=b.getOldVy();
                    //lets [say] total (horizontal) kinetic energy is evenly distributed between 3 masses
                    kineticEnergyX=Math.sqrt((aVx*aVx+bVx*bVx+currentMassVx*currentMassVx)/3.0*ENERGY_LEFT);
                    kineticEnergyY=Math.sqrt((aVy*aVy+bVy*bVy+currentMassVy*currentMassVy)/3.0*ENERGY_LEFT);
                    currentMass.setVx(kineticEnergyX);
                    a.setVx(0-kineticEnergyX); b.setVx(0-kineticEnergyX);
                    if(slopeOfLine>0){//old Slope!
                      if(resultOld==-1){ //on RHS
                        currentMass.setVy(0-kineticEnergyY);
                        a.setVy(kineticEnergyY); b.setVy(kineticEnergyY);
                      }//resultNew
                      else{
                        currentMass.setVy(kineticEnergyY);
                        a.setVy(0-kineticEnergyY); b.setVy(0-kineticEnergyY);
                      }//else resultNew!=1
                    }//if slope>0
                    else{ //slope<0
                      if(resultOld==-1){ //if on RHS
                       currentMass.setVy(kineticEnergyY);
                       a.setVy(0-kineticEnergyY); b.setVy(0-kineticEnergyY);
                      }//if on LHS
                      else{
                        currentMass.setVy(0-kineticEnergyY);
                        a.setVy(kineticEnergyY); b.setVy(kineticEnergyY);
                      }//else on RHS
                    }//slope <0
                  }// if
                  else{}// do nothing! :D
               //}// if collided?
            }//else if cmass1X!=cmass2X
            else if(cmass1X==cmass2X){ //cmass1X==cmass2X
              if(currentMassX>cmass1X){
              }//no collision, pruned
              else if(currentMassX>cmass1X-SPEED_LIMIT){
                currentMass.revertX();
                //currentMass.revertY();
                Mass a=model1.getMuscle(i).getMass1();
                Mass b=model1.getMuscle(i).getMass2();
                if(!touched){
                 touched=true;
                 firstContactPoint=currentMassX;
               }
              a.revertX(); b.revertX();
                currentMassVx=currentMass.getOldVx();
                currentMassVy=currentMass.getOldVy();
                double aVx=a.getOldVx();
                double bVx=b.getOldVx();
                //lets [say] total (horizontal) kinetic energy is evenly distributed between 3 masses
                kineticEnergyX=Math.sqrt((aVx*aVx+bVx*bVx+currentMassVx*currentMassVx)/3.0*ENERGY_LEFT);
                currentMass.setVx(kineticEnergyX);
                a.setVx(0-kineticEnergyX); b.setVx(0-kineticEnergyX);
              }// collided with vertical line
            }// else cmass1X==cmass2X
            else if(cmass1Y==cmass2Y){
             if(currentMassY>cmass1Y){
               //no collision, pruned
             }//
             else if(currentMassY<cmass1Y-SPEED_LIMIT){
               //currentMass.revertX();
               currentMass.revertY();
               Mass a=model1.getMuscle(i).getMass1();
               Mass b=model1.getMuscle(i).getMass2();
               if(!touched){
                 touched=true;
                 firstContactPoint=currentMassX;
               }
               a.revertY(); b.revertY();
               currentMassVy=currentMass.getOldVy();
               double aVy=a.getOldVy();
               double bVy=b.getOldVy();
               //lets [say] total (horizontal) kinetic energy is evenly distributed between 3 masses
               kineticEnergyY=Math.sqrt((aVy*aVy+bVy*bVy+currentMassVy*currentMassVy)/3.0*ENERGY_LEFT);
               currentMass.setVy(kineticEnergyY);
               a.setVy(0-kineticEnergyY); b.setVy(0-kineticEnergyY);
             }//else collided with horizontal line
           }//else cmass1Y==cmass2Y
        }//for muscles, model1
      }//for go through all mass2*/
  	}//else Narrow-phase

  }//Collision
  
  private int throwPointInLine(double x,double y, double yInter,double slope){
  	//y-mx-c, returns 1 if on the left 
  	int feedback=1;
  	double result=y-(slope*x)-yInter;
  	//System.out.println(slopeOfLine);
  	//System.out.println(result);
  	if(slope>0){
	  	if(result<0)
	  		feedback=-1;
	    else if(result==0)
	    	feedback=0;
  	}//if slope >0
  	else if(slope<0){
  		if(result>0)
  	  	feedback=-1;
  	  else if(result==0)
  	  	feedback=0;
  	}//else if
  	return feedback;
  }//throwPointInLine  
}//class