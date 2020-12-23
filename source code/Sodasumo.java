/**
 * @(#)Sodasumo.java
 *
 *
 * @author Kenneth "KeniF" Lam
 * @version 1.00 2007/10/15
 */
import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.awt.event.*;
import org.xml.sax.SAXException;
import java.io.*;

public class Sodasumo extends JFrame implements MouseListener,ItemListener,ActionListener, KeyListener{
  
  private static final String VERSION="SodaSumo Beta1.51";
  
  private static final Dimension GAME_DIMENSION = new Dimension(1000,350);
  private static final Dimension CP_DIMENSION=new Dimension(150,320);
  public static double GAME_WIDTH=GAME_DIMENSION.getWidth()-CP_DIMENSION.getWidth();
  private GameDraw newGameDraw=new GameDraw();
  //private JTextField model1Text,model2Text;
  private JButton loadButton,stopButton;
  private JCheckBox invertM1Button,invertM2Button;
  private Model model1,model2;
  private JMenuBar menuBar;
  private JMenu aboutMenu,helpMenu,loadMenu;
  private JMenuItem aboutItemSoda,aboutItemAuthor,aboutItemModels,faq,helpItem2,loadItem1,
    loadFromWeb,loadDir;
  private String[]xmlFiles;
  //private JList list1,list2;
  //private JScrollPane scrollPane;
  private boolean invertM1=false,invertM2=false;
  private JComboBox box1,box2,boxTime;//=new JComboBox();
  //private JComboBox box2=new JComboBox();
  //private JComboBox boxTime=new JComboBox();
  private JDialog dialog;
  private String modelLink;
  private File file1=new File("web1.xml");
  private File file2=new File("web2.xml");
  private FileDownloader downloader=new FileDownloader();
  private XmlParser xp1,xp2;
  private int webResult=0;
  
  private Sodasumo(){
    loadDirectory();
    menuBar=new JMenuBar();
    this.setJMenuBar(menuBar);
    menuBar.setBackground(Color.GRAY.brighter().brighter());
    setTitle(VERSION);
    
    aboutMenu=new JMenu("About");
    menuBar.add(aboutMenu);
    
    aboutItemSoda=new JMenuItem(VERSION);
    aboutMenu.add(aboutItemSoda);
    aboutItemSoda.addMouseListener(this);
    aboutItemAuthor=new JMenuItem("Author");
    aboutMenu.add(aboutItemAuthor);
    aboutItemAuthor.addMouseListener(this);
    //aboutItemModels=new JMenuItem("The Models");
    //aboutMenu.add(aboutItemModels);
    //aboutItemModels.addMouseListener(this);
    
    faq=new JMenuItem("FAQ");
    faq.addMouseListener(this);
    helpMenu=new JMenu("Help");
    helpMenu.add(faq);
    menuBar.add(helpMenu);
    
    Container contents=getContentPane();
    contents.setLayout(new BorderLayout());
    contents.setBackground(Color.WHITE);
    JPanel panel1=new JPanel();
    setSize(GAME_DIMENSION); //sets size of Window
    setResizable(false); //can't maximize
    contents.add(newGameDraw);
    JPanel cPanel=new JPanel();//Control Panel
    contents.add(cPanel,BorderLayout.EAST);
    //cPanel.setLayout(new GridLayout(10,1));
    cPanel.setBackground(Color.GRAY.brighter());
    cPanel.setPreferredSize(CP_DIMENSION);
    
    box1=new JComboBox(xmlFiles);
    box1.setMaximumRowCount(10);
    box1.setPrototypeDisplayValue("THIS IS A VERY LON");
    box1.setSelectedItem("daintywalker");
    box1.setEditable(true);
    box1.addActionListener(this);
    box1.setToolTipText("Load from Sodaplay: username/modelname then hit enter");
    cPanel.add(box1);
    
    invertM1Button=new JCheckBox("Invert Direction",false);
    invertM1Button.setBackground(Color.GRAY.brighter());
    invertM1Button.addItemListener(this);
    cPanel.add(invertM1Button);
    
    box2=new JComboBox(xmlFiles);
    box2.setMaximumRowCount(10);
    box2.setPrototypeDisplayValue("THIS IS A VERY LON");
    box2.setSelectedItem("KeniF_triangle");
    box2.setEditable(true);
    box2.addActionListener(this);
    box2.setToolTipText("Load from Sodaplay: username/modelname then hit enter");
    cPanel.add(box2);
    
    invertM2Button=new JCheckBox("Invert Direction",false);
    invertM2Button.setBackground(Color.GRAY.brighter());
    invertM2Button.addItemListener(this);
    cPanel.add(invertM2Button);
    
    loadButton=new JButton("  Start  ");
    loadButton.addMouseListener(this);
    loadButton.addKeyListener(this);
    loadButton.addActionListener(this);
    cPanel.add(loadButton);
    
    stopButton=new JButton("  Stop  ");
    stopButton.addMouseListener(this);
    stopButton.addKeyListener(this);
    stopButton.setVisible(false);
    stopButton.addActionListener(this);
    cPanel.add(stopButton);
    
    JLabel emptySpace=new JLabel("                                    ");
    JLabel emptySpace2=new JLabel("                                    ");
    JLabel eS3=new JLabel("                                    ");
    JLabel label=new JLabel("    Time Limit");
    cPanel.add(emptySpace);
    cPanel.add(emptySpace2);
    cPanel.add(eS3);
    cPanel.add(label);
    
    String[] times={"5","10","15","20","25","30","60"};
    boxTime=new JComboBox(times);
    boxTime.setMaximumRowCount(10);
    
    boxTime.setSelectedItem("15");
    boxTime.setEditable(false);
    boxTime.addActionListener(this);
    boxTime.setToolTipText("Set Time Limit");
    boxTime.setPrototypeDisplayValue("30");
    cPanel.add(boxTime);
    

    setDefaultCloseOperation(EXIT_ON_CLOSE);
    //pack(); //removed for customised window size
    setLocationRelativeTo(null);
    
  } // game constructor (GUI)

  public static void main(String [] args){
    Sodasumo newGame=new Sodasumo();
    newGame.setVisible(true);
    /*JOptionPane.showMessageDialog(newGame,
    "<html><font size=5 color=blue><b><u>What's New?</u></b></font></html>\n\n"+
    "You can now load directly from Sodaplay.\n"+
    "Simply input username/model_name\n"+
    "and then PRESS ENTER.\n"+
    "e.g. ed/daintywalker",  
    VERSION,JOptionPane.INFORMATION_MESSAGE);*/
  } // main
  
  private void loadDirectory(){
    try{
      String currentDir=System.getProperty("user.dir");
      File dir=new File(currentDir);
      FilenameFilter filter = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            return name.endsWith(".xml");}
      };
      xmlFiles=dir.list(filter);
      if(xmlFiles.length==0) throw new Exception();
      for(int i=0;i<xmlFiles.length;i++){//tested
        xmlFiles[i]=xmlFiles[i].substring(0,xmlFiles[i].length()-4);
        //System.out.println(xmlFiles[i]);
      }//for
    }//try
    catch(Exception e){
      JOptionPane.showMessageDialog(this,"No XML files in directory!!\n",
      "Error",JOptionPane.WARNING_MESSAGE);
     }//catch
  }//loadDirectory
  
  public void itemStateChanged(ItemEvent e){
    Object source=e.getItemSelectable();
    if(source==invertM1Button){
      invertM1=!invertM1;
      newGameDraw.invertM1();
    }//invertM1
    else if(source==invertM2Button){
      invertM2=!invertM2;
      newGameDraw.invertM2();
    }//invertM2
  }//
  
  public void actionPerformed(ActionEvent e){
        if(e.getSource()==box1){
        Object b=box1.getSelectedItem();
        if(box1.getSelectedIndex()==-1&&b.toString().contains("/")){//not in list
          box1.addItem(b);
          box2.addItem(b);
        }//checks if item already in list
        box1.showPopup();//must pop up after adding
      }//if box1
      
      else if(e.getSource()==box2){
        Object a=box2.getSelectedItem();
        if(box2.getSelectedIndex()==-1&&a.toString().contains("/")){//not in list
          box2.addItem(a);
          box1.addItem(a);
        }//checks if item already in list
      box2.showPopup();
      }//else if

      else if(e.getSource()==boxTime){
        newGameDraw.setTimeLimit(Double.parseDouble(boxTime.getSelectedItem().toString())*1000.0);
      }//else if
  }//actionPerformed
  
  public void mouseExited(MouseEvent e){  
  }//mouseExited

  public void mouseClicked(MouseEvent e){
  }//mouseClicked

  public void mouseEntered(MouseEvent e){
  }//mouseEntered
  public void keyTyped(KeyEvent e){
  }// keyTyped
  public void keyPressed(KeyEvent e){
    try{
    if ((e.getKeyCode()==KeyEvent.VK_ENTER||
      e.getKeyCode()==KeyEvent.VK_SPACE)&&loadButton.isVisible()) {
           String xmlFile1,xmlFile2;
       model1=null;
       xmlFile1=box1.getSelectedItem().toString();
       if(xmlFile1.contains("/")){
        file1=downloader.download(xmlFile1,file1);
        if(file1!=null)
          xp1=new XmlParser(file1);
        else{
          box1.removeItem(xmlFile1);
          box2.removeItem(xmlFile1);
          throw new Exception("Web location not found!");
         }//else
       }// if
       else
         xp1=new XmlParser(xmlFile1+".xml");
       if(xp1.getVerified()!=2)
         throw new Exception("Model 1 cannot be verified!");
       model2=null;
       xmlFile2=box2.getSelectedItem().toString();
       if(xmlFile2.contains("/")){
        if(xmlFile1.equals(xmlFile2))
          file2=file1;
        else{
          file2=downloader.download(xmlFile2,file2);
        }//else
        if(file2!=null)
          xp2=new XmlParser(file2);
        else{
          box2.removeItem(xmlFile2);
          box1.removeItem(xmlFile2);
          throw new Exception("Web location not found!");
         }//else
       }// if
       else
         xp2=new XmlParser(xmlFile2+".xml");
       if(xp2.getVerified()!=2)
         throw new Exception("Model 2 cannot be verified!");
       Model model1=xp1.getModel();
       model1.setName(box1.getSelectedItem().toString());
       Model model2=xp2.getModel();
       model2.setName(box2.getSelectedItem().toString());
       newGameDraw.provideModel1(model1);
       newGameDraw.provideModel2(model2);
       loadButton.setVisible(false);
       stopButton.setVisible(true);
       stopButton.grabFocus();
       box1.setEnabled(false);
       box2.setEnabled(false);
       //newGameDraw.setTimeLimit(Double.parseDouble(boxTime.getSelectedItem().toString())*1000.0);
       newGameDraw.startDraw();
       newGameDraw.init();
        }
    else if ((e.getKeyCode()==KeyEvent.VK_ENTER||
      e.getKeyCode()==KeyEvent.VK_SPACE)&&stopButton.isVisible()) {
        newGameDraw.pause();
        stopButton.setVisible(false);
        loadButton.setVisible(true);
        box1.setEnabled(true);
        loadButton.grabFocus();
        box2.setEnabled(true);
        }
    }//try
    catch(java.io.IOException f){
      JOptionPane.showMessageDialog(newGameDraw,"Please ensure the XML files are in the same directory.\n"+f,
      "Error During Load",JOptionPane.WARNING_MESSAGE);
    }//catch
    catch(SAXException s){
      JOptionPane.showMessageDialog(newGameDraw,"Invalid XML files.\n"+s,
      "Error During Load",JOptionPane.WARNING_MESSAGE);
    }//catch SAX
    catch(java.lang.NullPointerException ed){
      JOptionPane.showMessageDialog(newGameDraw,"Model contains objects not permitted in SodaSumo\n"+ed,
      "Error During Load",JOptionPane.WARNING_MESSAGE);
    }
    catch(Exception ds){
      JOptionPane.showMessageDialog(newGameDraw,"Error when loading models\n"+ds,
      "Error During Load",JOptionPane.WARNING_MESSAGE);
    }
    
  }// keyPressed
  public void keyReleased(KeyEvent e){
  }// keyReleased
  
  public void mousePressed(MouseEvent e){
    try{ ////////////////LOAD BUTTON//////////////////////  
      String xmlFile1,xmlFile2;
      if(e.getSource()==loadButton){
       model1=null;
       xmlFile1=box1.getSelectedItem().toString();
       if(xmlFile1.contains("/")){
        file1=downloader.download(xmlFile1,file1);
        if(file1!=null)
          xp1=new XmlParser(file1);
        else{
          box1.removeItem(xmlFile1);
          box2.removeItem(xmlFile1);
          throw new Exception("Web location not found!");
         }//else
       }// if
       else
         xp1=new XmlParser(xmlFile1+".xml");
       if(xp1.getVerified()!=2)
         throw new Exception("Model 1 cannot be verified!");
       model2=null;
       xmlFile2=box2.getSelectedItem().toString();
       if(xmlFile2.contains("/")){
        if(xmlFile1.equals(xmlFile2))
          file2=file1;
        else{
          file2=downloader.download(xmlFile2,file2);
        }//else
        if(file2!=null)
          xp2=new XmlParser(file2);
        else{
          box2.removeItem(xmlFile2);
          box1.removeItem(xmlFile2);
          throw new Exception("Web location not found!");
         }//else
       }// if
       else
         xp2=new XmlParser(xmlFile2+".xml");
       if(xp2.getVerified()!=2)
         throw new Exception("Model 2 cannot be verified!");
       Model model1=xp1.getModel();
       model1.setName(box1.getSelectedItem().toString());
       Model model2=xp2.getModel();
       model2.setName(box2.getSelectedItem().toString());
       newGameDraw.provideModel1(model1);
       newGameDraw.provideModel2(model2);
       loadButton.setVisible(false);
       stopButton.setVisible(true);
       box1.setEnabled(false);
       box2.setEnabled(false);
       //newGameDraw.setTimeLimit(Double.parseDouble(boxTime.getSelectedItem().toString())*1000.0);
       newGameDraw.startDraw();
       newGameDraw.init();
      }//if
      
      else if(e.getSource()==stopButton){
        newGameDraw.pause();
        loadButton.setVisible(true);
        stopButton.setVisible(false);
        box1.setEnabled(true);
        box2.setEnabled(true);
      }//else if stopButton
      
      else if(e.getSource()==aboutItemSoda){
        JOptionPane.showMessageDialog(newGameDraw,
        "<html><font size=5 color=blue><b><u>"+VERSION+"</u></b></font></html>\n\n"+
        "SodaSumo is a clone/extension to the physics game SodaRace.\n"+
        "While SodaRace allows users to import several models designed\n"+
        "in SodaConstructor for a competition in moving speed,\n"+
         "SodaSumo replicates the SodaConstructor physics engine\n"+
        "and takes the game further by implementing collision detection\n"+
        "between two models.\n"+
        "\nMore information at\n"+
         "<html><a href=\"http://sodaplay.com\">www.sodaplay.com</a></html>",
      "About",JOptionPane.INFORMATION_MESSAGE);
      }// aboutItem
      
      else if(e.getSource()==faq){
        JOptionPane.showMessageDialog(newGameDraw,
        "<html><font size=5 color=blue><b><u>FAQ</u></b></font><br><br><font size=4><u>How to create SodaSumo models?</u>"
        +"</font><br>SodaSumo models are created by SodaConstructor V2.<br>Go to sodaplay.com > Create > Constructor <br><br>"
        +"<font size=4><u>How can I import models from sodaplay.com?</u></font><br>"
        +"1) Copy the xml code from SodaConstructor and<br>  save it as .xml in a text editor.<br>"
        +"2) Download directly by entering USERNAME/MODELNAME<br>in the drop down boxes and press ENTER "
        +"to add them to the list.<br>e.g. ed/daintywalker<br><br>"
        +"You need an account on sodaplay.com to save your models.<br>You can also load other users' public models.</html>",
      "FAQ",JOptionPane.INFORMATION_MESSAGE);
      }//helpMenu
      
      else if(e.getSource()==aboutItemAuthor){
        JOptionPane.showMessageDialog(newGameDraw,
        "<html><font size=5 color=blue><b><u>Who made SodaSumo?</u></b></font></html>\n\n"+
        "(C) 2008 Kenneth \"KeniF\" Lam | keniflam@hotmail.com\n"+
        "Supervised by Dr. Mary McGee Wood\n\n"+
        "This program is written as my third year project\n"+
        "at the University of Manchester, United Kingdom.",
      "About",JOptionPane.INFORMATION_MESSAGE);
      }// aboutItem
      
    }//try
    
    catch(java.io.IOException f){
      JOptionPane.showMessageDialog(newGameDraw,"Please ensure the XML files are in the same directory\n"+f,
      "Error During Load",JOptionPane.WARNING_MESSAGE);
    }//catch
    catch(SAXException s){
      JOptionPane.showMessageDialog(newGameDraw,"Invalid XML files.\n"+s,
      "Error During Load",JOptionPane.WARNING_MESSAGE);
    }//catch SAX
    catch(java.lang.NullPointerException es){
      JOptionPane.showMessageDialog(newGameDraw,"Model contains objects not permitted in SodaSumo\n"+es,
      "Error During Load",JOptionPane.WARNING_MESSAGE);
    }
    catch(Exception ds){
      JOptionPane.showMessageDialog(newGameDraw,"Error when loading models\n"+ds,
      "Error During Load",JOptionPane.WARNING_MESSAGE);
    }
    
  }//mousePressed
  
  public void mouseReleased(MouseEvent e){
    //do nothing!
  }//mouseReleased
} // class game