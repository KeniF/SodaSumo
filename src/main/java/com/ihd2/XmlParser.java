package com.ihd2;

import com.ihd2.model.Mass;
import com.ihd2.model.Model;
import com.ihd2.model.Muscle;
import com.ihd2.model.Spring;
import org.xml.sax.ContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLReaderFactory;  //create an XMLReader from system defaults


public class XmlParser implements ContentHandler {

    private static InputSource source;
    private final int MODE_NONE = -1,
            MODE_GRAVITY = 0,
            MODE_FRICTION = 1,
            MODE_VX = 3,
            MODE_VY = 4,
            MODE_X = 5,
            MODE_Y = 6,
            MODE_MASS1 = 8,
            MODE_REST_LENGTH = 7,
            MODE_W_AMPLITUDE = 11,
            MODE_MASS2 = 9,
            MODE_M_AMPLITUDE = 10,
            MODE_M_PHASE = 15,
            MODE_SPRINGYNESS = 16,
            MODE_W_DIRECTION = 12,
            MODE_W_PHASE = 13,
            MODE_W_SPEED = 14,
            MODE_NODE = 17;

    private int mode;
    private int mass1, mass2;
    private Mass currentMass; // current mass being created
    private Spring currentSpring; // spring being created
    private Muscle currentMuscle; //muscle being created
    private boolean createMuscle = false, createWave = false;
    private final Model newModel; // World for holding masses and springs
    public int verified = 0; // to count the no. of strings verified
    //Strings used to verify Soda files
    private static final String VERIFY1 = "com.sodaplay.soda.constructor2.model.Model";
    private static final String VERIFY2 = "com.sodaplay.soda.constructor2.ConstructorApplication";

    public XmlParser(String path) throws java.io.IOException, SAXException {
        mode = MODE_NONE;
        newModel = new Model();
        source = new InputSource(new java.io.FileInputStream(path));
        XMLReader reader = XMLReaderFactory.createXMLReader();
        reader.setContentHandler(this);
        reader.parse(source);
    }

    public Model getModel() {
        return newModel;
    }

    public int getVerified() {
        return verified;
    }

    public void characters(char[] ch, int start, int length) {
        try {
            String readCh = new String(ch, start, length);
            readCh = readCh.trim();
            if (readCh.compareTo("") != 0) {
                switch (mode) {
                    case MODE_NONE:
                    case MODE_NODE:
                        break;

                    case MODE_FRICTION: {
                        newModel.setFriction(Double.parseDouble(readCh));
                        mode = MODE_NONE;
                        break;
                    }
                    case MODE_GRAVITY: {
                        newModel.setGravity(Double.parseDouble(readCh));
                        mode = MODE_NONE;
                        break;
                    }
                    case MODE_VX: {
                        currentMass.setVx(Double.parseDouble(readCh));
                        mode = MODE_NONE;
                        break;
                    }
                    case MODE_VY: {
                        currentMass.setVy(Double.parseDouble(readCh));
                        mode = MODE_NONE;
                        break;
                    }
                    case MODE_M_AMPLITUDE: {
                        currentMuscle.setAmplitude(Double.parseDouble(readCh));
                        mode = MODE_NONE;
                        break;
                    }
                    case MODE_W_AMPLITUDE: {
                        newModel.setWaveAmplitude(Double.parseDouble(readCh));
                        mode = MODE_NONE;
                        break;
                    }
                    case MODE_W_PHASE: {
                        newModel.setWavePhase(Double.parseDouble(readCh));
                        mode = MODE_NONE;
                        break;
                    }
                    case MODE_M_PHASE: {
                        currentMuscle.setPhase(Double.parseDouble(readCh));
                        mode = MODE_NONE;
                        break;
                    }
                    case MODE_W_SPEED: {
                        newModel.setWaveSpeed(Double.parseDouble(readCh));
                        mode = MODE_NONE;
                        break;
                    }
                    case MODE_X: {
                        currentMass.setX(Double.parseDouble(readCh));
                        mode = MODE_NONE;
                        break;
                    }
                    case MODE_W_DIRECTION: {
                        mode = MODE_NONE;
                        break;
                    }
                    case MODE_SPRINGYNESS: {
                        newModel.setSpringyness(Double.parseDouble(readCh));
                        mode = MODE_NONE;
                        break;
                    }
                    case MODE_Y: {
                        currentMass.setY(Double.parseDouble(readCh));
                        newModel.addMass(currentMass);//IMPORTANT: adds new mass to model
                        currentMass = null; //all masses created and saved to Model
                        mode = MODE_NONE;
                        break;
                    }
                    case MODE_REST_LENGTH: {
                        if (!createMuscle) {
                            currentSpring.setRestLength(Double.parseDouble(readCh));
                            currentSpring.setMass1(newModel.getMass(mass1));
                            mass1 = -1;
                            currentSpring.setMass2(newModel.getMass(mass2));
                            mass2 = -1;
                            newModel.addSpring(currentSpring);
                            currentSpring = null;
                        } else {
                            currentMuscle.setRestLength(Double.parseDouble(readCh));
                            currentMuscle.setMass1(newModel.getMass(mass1));
                            mass1 = -1;
                            currentMuscle.setMass2(newModel.getMass(mass2));
                            mass2 = -1;
                            newModel.addMuscle(currentMuscle); //adds spring to model
                            currentMuscle = null;
                        }
                        mode = MODE_NONE;
                        break;
                    }
                    default: {
                        throw new Exception("Unexpected error. Check XML file.");// unexpected state
                    }
                }
            }
        } catch(Exception e) {
            throw new NullPointerException();
        }
    }

    public void endDocument() {
    }

    public void endElement(String uri, String localName, String qName) {
    }

    public void endPrefixMapping(String prefix) {
    }

    public void ignorableWhitespace(char[] ch, int start, int length) {
    }

    public void processingInstruction(String target, String data) {
    }

    public void setDocumentLocator(Locator locator) {
    }

    public void skippedEntity(String name) {
    }

    public void startDocument() {
    }

    public void startElement(String uri, String localName, String qName, Attributes atts) {
        int length = atts.getLength();
        try {
            for (int i = 0; i < length; i++) {
                String aQname = atts.getQName(i);
                String aValue = atts.getValue(i);
                if (verified < 2) {
                    if (("type").equals(aQname) && (VERIFY1.equals(aValue))) {
                        verified++;
                    } else if (("type").equals(aQname) && VERIFY2.equals(aValue)) {
                        verified++;
                    }
                }
                else if (qName.equals("field")) {
                    if (aQname.equals("name")) {
                        if (mode == MODE_NONE) {
                            switch (aValue) {
                                case "friction":
                                    mode = MODE_FRICTION;
                                    break;
                                case "gravity":
                                    mode = MODE_GRAVITY;
                                    break;
                                case "vx":
                                    mode = MODE_VX;
                                    break;
                                case "vy":
                                    mode = MODE_VY;
                                    break;
                                case "x":
                                    mode = MODE_X;
                                    break;
                                case "y":
                                    mode = MODE_Y;
                                    break;
                                case "a":
                                    mode = MODE_MASS1;
                                    break;
                                case "b":
                                    mode = MODE_MASS2;
                                    break;
                                case "direction":
                                    mode = MODE_W_DIRECTION;
                                    break;
                                case "amplitude":
                                    if (createMuscle && !createWave) {
                                        mode = MODE_M_AMPLITUDE;
                                    } else if (createWave && !createMuscle) {
                                        mode = MODE_W_AMPLITUDE;
                                    }
                                    break;
                                case "phase":
                                    if (!createWave && createMuscle) {
                                        mode = MODE_M_PHASE;
                                    } else if (createWave && !createMuscle) {
                                        mode = MODE_W_PHASE;
                                    }
                                    break;
                                case "speed":
                                    mode = MODE_W_SPEED;
                                    break;
                                case "restLength":
                                    mode = MODE_REST_LENGTH;
                                    break;
                                case "wave":
                                    createWave = true;
                                    createMuscle = false;
                                    break;
                                case "springyness":
                                    mode = MODE_SPRINGYNESS;
                                    break;
                            }
                        }
                        else if (mode == MODE_NODE) {
                            if (aValue.equals("y")) {
                                mode = MODE_NONE;
                            }
                        } else {
                            throw new Exception("***************Unexpected State error in <field>***************");
                        }
                    }
                } else if (qName.equals("object")) {
                    if (aQname.equals("id") && aValue.startsWith("Mass")) {
                        currentMass = new Mass(Integer.parseInt(aValue.substring(5))); //Mass#? is the mass name(String)
                    }
                    else if (aQname.equals("id") && aValue.startsWith("Spring")) {
                        currentSpring = new Spring(Integer.parseInt(aValue.substring(7)));
                        createMuscle = false;
                    }
                    else if (aQname.equals("id") && aValue.startsWith("Muscle")) {
                        currentMuscle = new Muscle(Integer.parseInt(aValue.substring(7)));
                        createMuscle = true;
                    }
                    else if (aQname.equals("id") && aValue.startsWith("Node")) {
                        mode = MODE_NODE;
                    }
                }
                else if (qName.equals("ref")) {
                    if (aQname.equals("refid") && aValue.startsWith("Mass")) {
                        if (mode == MODE_MASS1) {
                            mass1 = Integer.parseInt(aValue.substring(5));
                            mode = MODE_NONE;
                        }
                        else if (mode == MODE_MASS2) {
                            mass2 = Integer.parseInt(aValue.substring(5));
                            mode = MODE_NONE;
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new NullPointerException();
        }
    }

    public void startPrefixMapping(String prefix, String uri) {
        //noop
    }
}