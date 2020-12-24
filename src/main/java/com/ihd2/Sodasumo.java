package com.ihd2;

import com.ihd2.model.Model;
import org.xml.sax.SAXException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FilenameFilter;

public class Sodasumo extends JFrame implements MouseListener, ItemListener, ActionListener, KeyListener {

    private static final String VERSION = "SodaSumo 1.6";

    private static final Dimension GAME_DIMENSION = new Dimension(1000, 350);
    private static final Dimension CP_DIMENSION = new Dimension(150, 320);
    public static double GAME_WIDTH = GAME_DIMENSION.getWidth() - CP_DIMENSION.getWidth();
    private final JButton loadButton;
    private final JButton stopButton;
    private final JCheckBox invertM1Button;
    private final JCheckBox invertM2Button;
    private final GameDraw newGameDraw = new GameDraw();
    private final JMenuItem aboutItemSoda;
    private final JMenuItem aboutItemAuthor;
    private String[] xmlFiles;
    private boolean invertM1 = false, invertM2 = false;
    private final JComboBox<Object> box1;
    private final JComboBox<Object> box2;
    private final JComboBox<String> boxTime;
    private XmlParser xp1, xp2;

    private Sodasumo() {
        loadDirectory();
        JMenuBar menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);
        menuBar.setBackground(Color.GRAY.brighter().brighter());
        setTitle(VERSION);

        JMenu aboutMenu = new JMenu("About");
        menuBar.add(aboutMenu);

        aboutItemSoda = new JMenuItem(VERSION);
        aboutMenu.add(aboutItemSoda);
        aboutItemSoda.addMouseListener(this);
        aboutItemAuthor = new JMenuItem("Author");
        aboutMenu.add(aboutItemAuthor);
        aboutItemAuthor.addMouseListener(this);

        Container contents = getContentPane();
        contents.setLayout(new BorderLayout());
        contents.setBackground(Color.WHITE);
        setSize(GAME_DIMENSION);
        setResizable(false);
        contents.add(newGameDraw);
        JPanel cPanel = new JPanel();
        contents.add(cPanel, BorderLayout.EAST);
        cPanel.setBackground(Color.GRAY.brighter());
        cPanel.setPreferredSize(CP_DIMENSION);

        box1 = new JComboBox<>(xmlFiles);
        box1.setMaximumRowCount(10);
        box1.setPrototypeDisplayValue("THIS IS A VERY LON");
        box1.setSelectedItem("daintywalker");
        box1.setEditable(true);
        box1.addActionListener(this);
        cPanel.add(box1);

        invertM1Button = new JCheckBox("Invert Direction", false);
        invertM1Button.setBackground(Color.GRAY.brighter());
        invertM1Button.addItemListener(this);
        cPanel.add(invertM1Button);

        box2 = new JComboBox<>(xmlFiles);
        box2.setMaximumRowCount(10);
        box2.setPrototypeDisplayValue("THIS IS A VERY LON");
        box2.setSelectedItem("KeniF_triangle");
        box2.setEditable(true);
        box2.addActionListener(this);
        cPanel.add(box2);

        invertM2Button = new JCheckBox("Invert Direction", false);
        invertM2Button.setBackground(Color.GRAY.brighter());
        invertM2Button.addItemListener(this);
        cPanel.add(invertM2Button);

        loadButton = new JButton("  Start  ");
        loadButton.addMouseListener(this);
        loadButton.addKeyListener(this);
        loadButton.addActionListener(this);
        cPanel.add(loadButton);

        stopButton = new JButton("  Stop  ");
        stopButton.addMouseListener(this);
        stopButton.addKeyListener(this);
        stopButton.setVisible(false);
        stopButton.addActionListener(this);
        cPanel.add(stopButton);

        JLabel emptySpace = new JLabel("                                    ");
        JLabel emptySpace2 = new JLabel("                                    ");
        JLabel eS3 = new JLabel("                                    ");
        JLabel label = new JLabel("    Time Limit");
        cPanel.add(emptySpace);
        cPanel.add(emptySpace2);
        cPanel.add(eS3);
        cPanel.add(label);

        String[] times = {"5", "10", "15", "20", "25", "30", "60"};
        boxTime = new JComboBox<>(times);
        boxTime.setMaximumRowCount(10);

        boxTime.setSelectedItem("15");
        boxTime.setEditable(false);
        boxTime.addActionListener(this);
        boxTime.setToolTipText("Set Time Limit");
        boxTime.setPrototypeDisplayValue("30");
        cPanel.add(boxTime);


        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

    }

    public static void main(String[] args) {
        Sodasumo newGame = new Sodasumo();
        newGame.setVisible(true);
    }

    private void loadDirectory() {
        try {
            String currentDir = System.getProperty("user.dir");
            File dir = new File(currentDir);
            FilenameFilter filter = (dir1, name) -> name.endsWith(".xml");
            xmlFiles = dir.list(filter);
            if (xmlFiles.length == 0) throw new Exception();
            for (int i = 0; i < xmlFiles.length; i++) {
                xmlFiles[i] = xmlFiles[i].substring(0, xmlFiles[i].length() - 4);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "No XML files in directory!!\n",
                    "Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    public void itemStateChanged(ItemEvent e) {
        ItemSelectable source = e.getItemSelectable();
        if (source == invertM1Button) {
            invertM1 = !invertM1;
            newGameDraw.invertM1();
        } else if (source == invertM2Button) {
            invertM2 = !invertM2;
            newGameDraw.invertM2();
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == box1) {
            Object b = box1.getSelectedItem();
            if (box1.getSelectedIndex() == -1 && b.toString().contains("/")) {
                box1.addItem(b);
                box2.addItem(b);
            }
            box1.showPopup();
        } else if (e.getSource() == box2) {
            Object a = box2.getSelectedItem();
            if (box2.getSelectedIndex() == -1 && a.toString().contains("/")) {
                box2.addItem(a);
                box1.addItem(a);
            }
            box2.showPopup();
        } else if (e.getSource() == boxTime) {
            newGameDraw.setTimeLimit(Double.parseDouble(boxTime.getSelectedItem().toString()) * 1000.0);
        }
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        try {
            if ((e.getKeyCode() == KeyEvent.VK_ENTER ||
                    e.getKeyCode() == KeyEvent.VK_SPACE) && loadButton.isVisible()) {
                String xmlFile1, xmlFile2;
                xmlFile1 = box1.getSelectedItem().toString();
                xp1 = new XmlParser(xmlFile1 + ".xml");
                if (xp1.getVerified() != 2)
                    throw new Exception("Model 1 cannot be verified!");
                xmlFile2 = box2.getSelectedItem().toString();
                xp2 = new XmlParser(xmlFile2 + ".xml");
                if (xp2.getVerified() != 2)
                    throw new Exception("Model 2 cannot be verified!");
                Model model1 = xp1.getModel();
                model1.setName(box1.getSelectedItem().toString());
                Model model2 = xp2.getModel();
                model2.setName(box2.getSelectedItem().toString());
                newGameDraw.provideModel1(model1);
                newGameDraw.provideModel2(model2);
                loadButton.setVisible(false);
                stopButton.setVisible(true);
                stopButton.grabFocus();
                box1.setEnabled(false);
                box2.setEnabled(false);
                newGameDraw.startDraw();
                newGameDraw.init();
            } else if ((e.getKeyCode() == KeyEvent.VK_ENTER ||
                    e.getKeyCode() == KeyEvent.VK_SPACE) && stopButton.isVisible()) {
                newGameDraw.pause();
                stopButton.setVisible(false);
                loadButton.setVisible(true);
                box1.setEnabled(true);
                loadButton.grabFocus();
                box2.setEnabled(true);
            }
        } catch (java.io.IOException f) {
            JOptionPane.showMessageDialog(newGameDraw, "Please ensure the XML files are in the same directory.\n" + f,
                    "Error During Load", JOptionPane.WARNING_MESSAGE);
        } catch (SAXException s) {
            JOptionPane.showMessageDialog(newGameDraw, "Invalid XML files.\n" + s,
                    "Error During Load", JOptionPane.WARNING_MESSAGE);
        } catch (java.lang.NullPointerException ed) {
            JOptionPane.showMessageDialog(newGameDraw, "Model contains objects not permitted in SodaSumo\n" + ed,
                    "Error During Load", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ds) {
            JOptionPane.showMessageDialog(newGameDraw, "Error when loading models\n" + ds,
                    "Error During Load", JOptionPane.WARNING_MESSAGE);
        }

    }

    public void keyReleased(KeyEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        try {
            String xmlFile1, xmlFile2;
            if (e.getSource() == loadButton) {
                xmlFile1 = box1.getSelectedItem().toString();
                xp1 = new XmlParser(xmlFile1 + ".xml");
                if (xp1.getVerified() != 2)
                    throw new Exception("Model 1 cannot be verified!");
                xmlFile2 = box2.getSelectedItem().toString();
                xp2 = new XmlParser(xmlFile2 + ".xml");
                if (xp2.getVerified() != 2)
                    throw new Exception("Model 2 cannot be verified!");
                Model model1 = xp1.getModel();
                model1.setName(box1.getSelectedItem().toString());
                Model model2 = xp2.getModel();
                model2.setName(box2.getSelectedItem().toString());
                newGameDraw.provideModel1(model1);
                newGameDraw.provideModel2(model2);
                loadButton.setVisible(false);
                stopButton.setVisible(true);
                box1.setEnabled(false);
                box2.setEnabled(false);
                newGameDraw.startDraw();
                newGameDraw.init();
            } else if (e.getSource() == stopButton) {
                newGameDraw.pause();
                loadButton.setVisible(true);
                stopButton.setVisible(false);
                box1.setEnabled(true);
                box2.setEnabled(true);
            } else if (e.getSource() == aboutItemSoda) {
                JOptionPane.showMessageDialog(newGameDraw,
                        "<html><font size=5 color=blue><b><u>" + VERSION + "</u></b></font></html>\n\n" +
                                "SodaSumo is a clone/extension to the physics game SodaRace.\n" +
                                "While SodaRace allows users to import several models designed\n" +
                                "in SodaConstructor for a competition in moving speed,\n" +
                                "SodaSumo replicates the SodaConstructor physics engine\n" +
                                "and takes the game further by implementing collision detection\n" +
                                "between two models.\n" +
                                "\nMore information at\n" +
                                "<html><a href=\"http://sodaplay.com\">www.sodaplay.com</a></html>",
                        "About", JOptionPane.INFORMATION_MESSAGE);
            } else if (e.getSource() == aboutItemAuthor) {
                JOptionPane.showMessageDialog(newGameDraw,
                        "<html><font size=5 color=blue><b><u>Who made SodaSumo?</u></b></font></html>\n\n" +
                                "(C) 2008, 2020 Kenneth \"KeniF\" Lam | kenif.lam@gmail.com\n\n" +
                                "This program was started as my third year project\n" +
                                "at the University of Manchester, United Kingdom.\n" +
                                "Supervised by Dr. Mary McGee Wood",
                        "About", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (java.io.IOException f) {
            JOptionPane.showMessageDialog(newGameDraw, "Please ensure the XML files are in the same directory\n" + f,
                    "Error During Load", JOptionPane.WARNING_MESSAGE);
        } catch (SAXException s) {
            JOptionPane.showMessageDialog(newGameDraw, "Invalid XML files.\n" + s,
                    "Error During Load", JOptionPane.WARNING_MESSAGE);
        } catch (java.lang.NullPointerException es) {
            JOptionPane.showMessageDialog(newGameDraw, "Model contains objects not permitted in SodaSumo\n" + es,
                    "Error During Load", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ds) {
            JOptionPane.showMessageDialog(newGameDraw, "Error when loading models\n" + ds,
                    "Error During Load", JOptionPane.WARNING_MESSAGE);
        }
    }

    public void mouseReleased(MouseEvent e) {
    }
}