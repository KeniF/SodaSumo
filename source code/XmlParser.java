import org.xml.sax.ContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.InputSource;
//import org.xml.sax.ErrorHandler;
import org.xml.sax.helpers.XMLReaderFactory;  //create an XMLReader from system defaults
import java.io.File;

public class XmlParser implements ContentHandler{

//private static String parserClass="org.apache.xerces.parsers.SAXParser";
private static InputSource source;
// Different states for different actions MODE_NONE: do nothing
private final int MODE_NONE=-1,MODE_GRAVITY=0,MODE_FRICTION=1,
MODE_VX=3,MODE_VY=4,MODE_X=5,MODE_Y=6,MODE_MASS1=8,
MODE_REST_LENGTH=7,MODE_W_AMPLITUDE=11,MODE_MASS2=9,
MODE_M_AMPLITUDE=10,MODE_M_PHASE=15,MODE_SPRINGYNESS=16,
MODE_W_DIRECTION=12,MODE_W_PHASE=13,MODE_W_SPEED=14,
MODE_NODE=17;

private int mode;
private int mass1,mass2;
private Mass currentMass; // current mass being created
private Spring currentSpring; // spring being created
private Muscle currentMuscle; //muscle being created
private boolean createMuscle=false,createWave=false;
private Model newModel; // World for holding masses and springs
public int verified=0; // to count the no. of strings verified
//Strings used to verify Soda files
private String VERIFY1="com.sodaplay.soda.constructor2.model.Model", 
VERIFY2="com.sodaplay.soda.constructor2.ConstructorApplication";

  public XmlParser(String path)throws java.io.FileNotFoundException,
   java.io.IOException,SAXException{
    //super();
    mode=MODE_NONE;
    newModel=new Model();
    source=new InputSource(new java.io.FileInputStream(new java.io.File(path)));
    XMLReader reader=XMLReaderFactory.createXMLReader();//(parserClass)now using default
    reader.setContentHandler(this);
    reader.parse(source);
  }//constructor
  
  //Allows downloaded File object to be used(no need for files in directory)
  public XmlParser(File file)throws java.io.FileNotFoundException,
   java.io.IOException,SAXException{
    //super();
    mode=MODE_NONE;
    newModel=new Model();
    source=new InputSource(new java.io.FileInputStream(file));
    XMLReader reader=XMLReaderFactory.createXMLReader();//(parserClass)now using default
    reader.setContentHandler(this);
    reader.parse(source);
  }//constructor
  
  
  public Model getModel(){
    return newModel;
  }//getModel
  
  public int getVerified(){
    return verified;
  }//getVerified
  
  public void characters(char[] ch, int start, int length){
    try{
      String readCh=new String(ch,start,length);
      readCh=readCh.trim(); //remove useless white spaces
      if(readCh.compareTo("")!=0){ //if string is not white space / new line
        //System.out.println("<Character>"+readCh);
        switch(mode){
          case MODE_NONE:{ 
            //System.out.println("MODE_NONE: Character ignored");
            break;// do nothing
          }// case MODE_NONE
          case MODE_NODE:{
            //do nothing'
            break;
          }//MODE_NODE
          case MODE_FRICTION:{
            newModel.setFriction(Double.parseDouble(readCh));
            mode=MODE_NONE;
            break;
          }//case MODE_FRICTION
          case MODE_GRAVITY:{
            newModel.setGravity(Double.parseDouble(readCh));
            mode=MODE_NONE;
            break;
          }// case MODE_GRAVITY
          case MODE_VX:{
            currentMass.setVx(Double.parseDouble(readCh));
            mode=MODE_NONE;
            break;
          }// MODE_VX
          case MODE_VY:{
            currentMass.setVy(Double.parseDouble(readCh));
            mode=MODE_NONE;
            break;
          }// MODE_VY
          case MODE_M_AMPLITUDE:{
            currentMuscle.setAmplitude(Double.parseDouble(readCh));
            mode=MODE_NONE;
            break;
          }// MODE_M_AMPLITUDE
          case MODE_W_AMPLITUDE:{
            newModel.setWaveAmplitude(Double.parseDouble(readCh));
            mode=MODE_NONE;
            break;
          }//MODE_W_AMPLUTIDE
          case MODE_W_PHASE:{
            newModel.setWavePhase(Double.parseDouble(readCh));
            mode=MODE_NONE;
            break;
          }// MODE_W_PHASE
          case MODE_M_PHASE:{
            currentMuscle.setPhase(Double.parseDouble(readCh));
            mode=MODE_NONE;
            break;
          }//MODE_M_PHASE
          case MODE_W_SPEED:{
            newModel.setWaveSpeed(Double.parseDouble(readCh));
            mode=MODE_NONE;
            break;
          }// MODE_W_SPEED
          case MODE_X:{
            currentMass.setX(Double.parseDouble(readCh));
            mode=MODE_NONE;
            break;
          }// MODE_X
          case MODE_W_DIRECTION:{
            String s=new String(readCh);
            if(s.equals("forward"))
              newModel.setWaveDirection(1);
            else 
              newModel.setWaveDirection(0);
            mode=MODE_NONE;
            break;
          }// MODE_W_DIRECTION
          case MODE_SPRINGYNESS:{
            newModel.setSpringyness(Double.parseDouble(readCh));
            mode=MODE_NONE;
            break;
          }//mode_springyness
          case MODE_Y:{
            currentMass.setY(Double.parseDouble(readCh));
            //System.out.println(currentMass.toString());
            newModel.addMass(currentMass);//IMPORTANT: adds new mass to model
            currentMass=null; //all masses created and saved to Model
            //System.out.println("There were "+newModel.totalNoOfMass()+" masses");
            mode=MODE_NONE;
            break;
          }// MODE_Y
          case MODE_REST_LENGTH:{
            if(!createMuscle){
              currentSpring.setRestLength(Double.parseDouble(readCh));
              //System.out.println(currentSpring.toString());
              currentSpring.setMass1(newModel.getMass(mass1));
              mass1=-1;
              currentSpring.setMass2(newModel.getMass(mass2));
              mass2=-1;
              newModel.addSpring(currentSpring); //adds spring to model
              currentSpring=null; //clears current/last spring
              //System.out.println("There were "+newModel.totalNoOfSpring()+" springs");
            }//!createMuscle
            else if(createMuscle){
              currentMuscle.setRestLength(Double.parseDouble(readCh));
              currentMuscle.setMass1(newModel.getMass(mass1));
              mass1=-1;
              currentMuscle.setMass2(newModel.getMass(mass2));
              mass2=-1;
              newModel.addMuscle(currentMuscle); //adds spring to model
              currentMuscle=null; //clears current/last spring
              //System.out.println("There were "+newModel.totalNoOfMuscle()+" muscles");
            }//createMuscle
            mode=MODE_NONE;
            break;
          }// REST_LENGTH
          default:{ 
            throw new Exception("Unexpected error. Check XML file.");// unexpected state
          }//default
        }//switch
      }//if
    }//try

    catch(Exception e){
      throw new NullPointerException();
    }//catch
  }//characters
  
  public void endDocument(){
    //System.out.println("Document Ended!");
  }//endDocument
  
  public void endElement(String uri, String localName, String qName){
  }//endElement
  
  public void endPrefixMapping(String prefix){
    //System.out.println("End prefix:"+prefix);
  }//endPrefixMapping
  
  public void ignorableWhitespace(char[] ch, int start, int length){
    //System.out.println("*");
  }//ignorableWhitespace
  
  public void processingInstruction(String target, String data){
    //System.out.println("<Target>"+target+" <Data>"+data);
  }// processingInstructions
  
  public void setDocumentLocator(Locator locator){
    //System.out.println("<Locator>"+locator);
  }//setDocumentLocator
 
  public void skippedEntity(String name){
    //System.out.println("Skipped:"+name);
  }//skippedEntity
  
  public void startDocument(){
    //System.out.println("Document started!");
  }//startDocument
  
  public void startElement(String uri, String localName, String qName, Attributes atts){
    //System.out.print("<"+localName+">");
    int length=atts.getLength();
    try{
      for(int i=0;i<length;i++){
        String aQname=atts.getQName(i);
        String aValue=atts.getValue(i);
        //System.out.println("<ATTR>"+aQname+
         // "<VALUE>"+aValue);
        if(verified<2){ // not verified yet
          if((new String("type")).equals(aQname)&&(VERIFY1.equals(aValue))){
           //System.out.println("VERIFIED!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n");
           verified++;
          }//if new string
          else if((new String("type")).equals(aQname)&&VERIFY2.equals(aValue)){
            //System.out.println("VERIFIED!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n");
            verified++;
          }//else if
        }//if(!verified)
        else if(qName.equals("field")){ //verified
          if(aQname.equals("name")){
            if(mode==MODE_NONE){
              if(aValue.equals("friction")){
                mode=MODE_FRICTION;
                //System.out.println("--------------[FRICTION]--------------");
              }//if friction
              else if(aValue.equals("gravity")){
                mode=MODE_GRAVITY;
                //System.out.println("--------------[GRAVITY]--------------");
              }//else if
              else if(aValue.equals("vx")){
                mode=MODE_VX;
                //System.out.println("--------------[VX]--------------");
              }//else if
              else if(aValue.equals("vy")){
                mode=MODE_VY;
                //System.out.println("--------------[VY]--------------");
              }//else if
              else if(aValue.equals("x")){
                mode=MODE_X;
                //System.out.println("--------------[X]--------------");
              }//else if
              else if(aValue.equals("y")){
                mode=MODE_Y;
                //System.out.println("--------------[Y]--------------");
              }//else if
              else if(aValue.equals("a")){
                mode=MODE_MASS1;
                //System.out.println("--------------[MODE_MASS1]--------------");
              }// else if
              else if(aValue.equals("b")){
                mode=MODE_MASS2;
                //System.out.println("--------------[MODE_MASS2]--------------");
              }//else if
              else if(aValue.equals("direction")){
                mode=MODE_W_DIRECTION;
                //System.out.println("--------------[MODE_W_DIRECTION]---------");
              }//else if
              else if(aValue.equals("amplitude")){
                if(createMuscle==true&&createWave==false){                    
                  mode=MODE_M_AMPLITUDE;
                  //System.out.println("-------------[MODE_M_AMPLITUDE]------------");
                }//if
                else if(createWave==true&&createMuscle==false){
                  mode=MODE_W_AMPLITUDE;
                  //System.out.println("-------------[MODE_W_AMPLITUDE]------------");
                }//else if createWave
              }//else if amplitude
              else if(aValue.equals("phase")){
                if(createWave==false&&createMuscle==true){
                  mode=MODE_M_PHASE;
                  //System.out.println("-------------[MODE_M_PHASE]-----------");
                }//if 
                else if(createWave==true&&createMuscle==false){
                  mode=MODE_W_PHASE;
                  //System.out.println("-------------MODE_W_PHASE------------");
                }//else if
              }// else if phase
              else if(aValue.equals("speed")){
                mode=MODE_W_SPEED;
                //System.out.println("-------------MODE_W_SPEED----------");
              }//else if speed
              else if(aValue.equals("restLength")){
                mode=MODE_REST_LENGTH;
                //System.out.println("--------------[MODE_REST_LENGTH]------------");
              }//else if restLength
              else if(aValue.equals("wave")){
                createWave=true; createMuscle=false;
              }//else if wave
              else if(aValue.equals("springyness")){
                mode=MODE_SPRINGYNESS;
                //System.out.println("----------MODE_SPRINGYNESS---------");
              }//else if springyness
            }// if mode==MODE_NONE
            else if(mode==MODE_NODE){
              if(aValue.equals("y")){
                mode=MODE_NONE;
              }//"y"
              else if(aValue.equals("x")){
                //do nothing}
              }// else if "x"
            }//
            else{
              throw new Exception("***************Unexpected State error in <field>***************");
            }// else
          }//if name
        }// else if field
        else if(qName.equals("object")){ //object creation
          if(aQname.equals("id")&&aValue.substring(0,4).equals("Mass")){
            //System.out.println("Mass"+Integer.parseInt(aValue.substring(5,aValue.length())));
            currentMass=new Mass(Integer.parseInt(aValue.substring(5,aValue.length()))); //Mass#? is the mass name(String)
          }// if MASS
          else if(aQname.equals("id")&&aValue.substring(0,6).equals("Spring")){
            //System.out.println("Spring"+aValue.substring(7,aValue.length()));
            currentSpring=new Spring(Integer.parseInt(aValue.substring(7,aValue.length())));
            createMuscle=false;
          }//else if Spring
          else if(aQname.equals("id")&&aValue.substring(0,6).equals("Muscle")){
            //System.out.println("Muscle"+aValue.substring(7,aValue.length()));
            currentMuscle=new Muscle(Integer.parseInt(aValue.substring(7,aValue.length())));
            createMuscle=true;
          }//else if Muscle
          else if(aQname.equals("id")&&aValue.substring(0,4).equals("Node")){
            mode=MODE_NODE;
          }//
        }//else if (object)
        else if(qName.equals("ref")){
          if(aQname.equals("refid")&&aValue.substring(0,4).equals("Mass")){
            if(!createMuscle){
              if(mode==MODE_MASS1){
                mass1=Integer.parseInt(aValue.substring(5,aValue.length()));
                mode=MODE_NONE;
              }//MODE_MASS1
              else if(mode==MODE_MASS2){
                mass2=Integer.parseInt(aValue.substring(5,aValue.length()));
                mode=MODE_NONE;
              }//MODE_MASS2
            }//if !createMuscle
            else{ //createMucsle
              if(mode==MODE_MASS1){
                mass1=Integer.parseInt(aValue.substring(5,aValue.length()));
                mode=MODE_NONE;
              }//MODE_MASS1
              else if(mode==MODE_MASS2){
                mass2=Integer.parseInt(aValue.substring(5,aValue.length()));
                mode=MODE_NONE;
              }//MODE_MASS2                
            }//else (createMuscle)
          }//if refid
        }//else if (ref)
      }//for
    }//try
    catch(Exception e){
      throw new NullPointerException();
    }//catch
  }//startElement
  
  public void startPrefixMapping(String prefix, String uri){
    //System.out.println("<Start Prefix>"+prefix+"<uri>"+uri);
  }//startPrefixMapping
}//class 