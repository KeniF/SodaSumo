package com.ihd2

import com.ihd2.model.Mass
import com.ihd2.model.Muscle
import java.lang.NullPointerException
import com.ihd2.model.Model
import com.ihd2.model.Spring
import org.xml.sax.Attributes
import org.xml.sax.ContentHandler
import org.xml.sax.InputSource
import org.xml.sax.Locator
import java.io.FileInputStream
import org.xml.sax.helpers.XMLReaderFactory
import java.lang.Exception

//create an XMLReader from system defaults
class XmlParser(path: String?) : ContentHandler {
    private val MODE_NONE = -1
    private val MODE_GRAVITY = 0
    private val MODE_FRICTION = 1
    private val MODE_VX = 3
    private val MODE_VY = 4
    private val MODE_X = 5
    private val MODE_Y = 6
    private val MODE_MASS1 = 8
    private val MODE_REST_LENGTH = 7
    private val MODE_W_AMPLITUDE = 11
    private val MODE_MASS2 = 9
    private val MODE_M_AMPLITUDE = 10
    private val MODE_M_PHASE = 15
    private val MODE_SPRINGYNESS = 16
    private val MODE_W_DIRECTION = 12
    private val MODE_W_PHASE = 13
    private val MODE_W_SPEED = 14
    private val MODE_NODE = 17
    private var mode: Int
    private var mass1 = 0
    private var mass2 = 0
    private var currentMass: Mass? = null
    private var currentSpring: Spring? = null
    private var currentMuscle: Muscle? = null
    private var createMuscle = false
    private var createWave = false
    private var source: InputSource

    val model // World for holding masses and springs
            : Model
    var verified = 0 // to count the no. of strings verified
    override fun characters(ch: CharArray, start: Int, length: Int) {
        try {
            var readCh = String(ch, start, length)
            readCh = readCh.trim { it <= ' ' }
            if (readCh.compareTo("") != 0) {
                when (mode) {
                    MODE_NONE, MODE_NODE -> {
                    }
                    MODE_FRICTION -> {
                        model.friction = readCh.toDouble()
                        mode = MODE_NONE
                    }
                    MODE_GRAVITY -> {
                        model.gravity = readCh.toDouble()
                        mode = MODE_NONE
                    }
                    MODE_VX, MODE_VY -> {
                        mode = MODE_NONE
                    }
                    MODE_M_AMPLITUDE -> {
                        currentMuscle!!.amplitude = readCh.toDouble()
                        mode = MODE_NONE
                    }
                    MODE_W_AMPLITUDE -> {
                        model.waveAmplitude = readCh.toDouble()
                        mode = MODE_NONE
                    }
                    MODE_W_PHASE -> {
                        model.wavePhase = readCh.toDouble()
                        mode = MODE_NONE
                    }
                    MODE_M_PHASE -> {
                        currentMuscle!!.phase = readCh.toDouble()
                        mode = MODE_NONE
                    }
                    MODE_W_SPEED -> {
                        model.waveSpeed = readCh.toDouble()
                        mode = MODE_NONE
                    }
                    MODE_X -> {
                        currentMass!!.setX(readCh.toDouble())
                        mode = MODE_NONE
                    }
                    MODE_W_DIRECTION -> {
                        mode = MODE_NONE
                    }
                    MODE_SPRINGYNESS -> {
                        model.springyness = readCh.toDouble()
                        mode = MODE_NONE
                    }
                    MODE_Y -> {
                        currentMass!!.setY(readCh.toDouble())
                        model.addMass(currentMass!!) //IMPORTANT: adds new mass to model
                        currentMass = null //all masses created and saved to Model
                        mode = MODE_NONE
                    }
                    MODE_REST_LENGTH -> {
                        if (!createMuscle) {
                            currentSpring!!.restLength = readCh.toDouble()
                            currentSpring!!.mass1 = model.getMass(mass1)
                            mass1 = -1
                            currentSpring!!.mass2 = model.getMass(mass2)
                            mass2 = -1
                            model.addSpring(currentSpring!!)
                            currentSpring = null
                        } else {
                            currentMuscle!!.restLength = readCh.toDouble()
                            currentMuscle!!.mass1 = model.getMass(mass1)
                            mass1 = -1
                            currentMuscle!!.mass2 = model.getMass(mass2)
                            mass2 = -1
                            model.addMuscle(currentMuscle!!) //adds spring to model
                            currentMuscle = null
                        }
                        mode = MODE_NONE
                    }
                    else -> {
                        throw Exception("Unexpected error. Check XML file.") // unexpected state
                    }
                }
            }
        } catch (e: Exception) {
            throw NullPointerException()
        }
    }

    override fun endDocument() {}
    override fun endElement(uri: String, localName: String, qName: String) {}
    override fun endPrefixMapping(prefix: String) {}
    override fun ignorableWhitespace(ch: CharArray, start: Int, length: Int) {}
    override fun processingInstruction(target: String, data: String) {}
    override fun setDocumentLocator(locator: Locator) {}
    override fun skippedEntity(name: String) {}
    override fun startDocument() {}
    override fun startElement(uri: String, localName: String, qName: String, atts: Attributes) {
        val length = atts.length
        try {
            for (i in 0 until length) {
                val aQname = atts.getQName(i)
                val aValue = atts.getValue(i)
                if (verified < 2) {
                    if ("type" == aQname && VERIFY1 == aValue) {
                        verified++
                    } else if ("type" == aQname && VERIFY2 == aValue) {
                        verified++
                    }
                } else if (qName == "field") {
                    if (aQname == "name") {
                        if (mode == MODE_NONE) {
                            when (aValue) {
                                "friction" -> mode = MODE_FRICTION
                                "gravity" -> mode = MODE_GRAVITY
                                "vx" -> mode = MODE_VX
                                "vy" -> mode = MODE_VY
                                "x" -> mode = MODE_X
                                "y" -> mode = MODE_Y
                                "a" -> mode = MODE_MASS1
                                "b" -> mode = MODE_MASS2
                                "direction" -> mode = MODE_W_DIRECTION
                                "amplitude" -> if (createMuscle && !createWave) {
                                    mode = MODE_M_AMPLITUDE
                                } else if (createWave && !createMuscle) {
                                    mode = MODE_W_AMPLITUDE
                                }
                                "phase" -> if (!createWave && createMuscle) {
                                    mode = MODE_M_PHASE
                                } else if (createWave && !createMuscle) {
                                    mode = MODE_W_PHASE
                                }
                                "speed" -> mode = MODE_W_SPEED
                                "restLength" -> mode = MODE_REST_LENGTH
                                "wave" -> {
                                    createWave = true
                                    createMuscle = false
                                }
                                "springyness" -> mode = MODE_SPRINGYNESS
                            }
                        } else if (mode == MODE_NODE) {
                            if (aValue == "y") {
                                mode = MODE_NONE
                            }
                        } else {
                            throw Exception("***************Unexpected State error in <field>***************")
                        }
                    }
                } else if (qName == "object") {
                    if (aQname == "id" && aValue.startsWith("Mass")) {
                        currentMass = Mass(aValue.substring(5).toInt()) //Mass#? is the mass name(String)
                    } else if (aQname == "id" && aValue.startsWith("Spring")) {
                        currentSpring = Spring(aValue.substring(7).toInt())
                        createMuscle = false
                    } else if (aQname == "id" && aValue.startsWith("Muscle")) {
                        currentMuscle = Muscle(aValue.substring(7).toInt())
                        createMuscle = true
                    } else if (aQname == "id" && aValue.startsWith("Node")) {
                        mode = MODE_NODE
                    }
                } else if (qName == "ref") {
                    if (aQname == "refid" && aValue.startsWith("Mass")) {
                        if (mode == MODE_MASS1) {
                            mass1 = aValue.substring(5).toInt()
                            mode = MODE_NONE
                        } else if (mode == MODE_MASS2) {
                            mass2 = aValue.substring(5).toInt()
                            mode = MODE_NONE
                        }
                    }
                }
            }
        } catch (e: Exception) {
            throw NullPointerException()
        }
    }

    override fun startPrefixMapping(prefix: String, uri: String) {
        //noop
    }

    companion object {
        //Strings used to verify Soda files
        private const val VERIFY1 = "com.sodaplay.soda.constructor2.model.Model"
        private const val VERIFY2 = "com.sodaplay.soda.constructor2.ConstructorApplication"
    }

    init {
        mode = MODE_NONE
        model = Model()
        source = InputSource(FileInputStream(path))
        val reader = XMLReaderFactory.createXMLReader()
        reader.contentHandler = this
        reader.parse(source)
    }
}