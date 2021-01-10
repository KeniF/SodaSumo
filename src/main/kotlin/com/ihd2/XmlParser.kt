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
    
    enum class ParseMode {
        NONE,
        GRAVITY,
        FRICTION,
        VX,
        VY,
        X,
        Y,
        MASS1,
        REST_LENGTH,
        W_AMPLITUDE,
        MASS2,
        M_AMPLITUDE,
        M_PHASE,
        SPRINGYNESS,
        W_DIRECTION,
        W_PHASE,
        W_SPEED,
        NODE
    }
    private var mode: ParseMode
    private var mass1 = 0
    private var mass2 = 0
    private lateinit var currentMass: Mass
    private lateinit var currentSpring: Spring
    private lateinit var currentMuscle: Muscle
    private var createMuscle = false
    private var createWave = false
    private var source: InputSource
    private var masses: MutableMap<Int, Mass> = HashMap()

    val model: Model
    var verified = 0 // to count the no. of strings verified
    override fun characters(ch: CharArray, start: Int, length: Int) {
        try {
            var readCh = String(ch, start, length)
            readCh = readCh.trim { it <= ' ' }
            if (readCh.compareTo("") != 0) {
                when (mode) {
                    ParseMode.NONE, ParseMode.NODE -> {
                    }
                    ParseMode.FRICTION -> {
                        model.friction = readCh.toDouble()
                        mode = ParseMode.NONE
                    }
                    ParseMode.GRAVITY -> {
                        model.gravity = readCh.toDouble()
                        mode = ParseMode.NONE
                    }
                    ParseMode.VX, ParseMode.VY -> {
                        mode = ParseMode.NONE
                    }
                    ParseMode.M_AMPLITUDE -> {
                        currentMuscle.amplitude = readCh.toDouble()
                        mode = ParseMode.NONE
                    }
                    ParseMode.W_AMPLITUDE -> {
                        model.waveAmplitude = readCh.toDouble()
                        mode = ParseMode.NONE
                    }
                    ParseMode.W_PHASE -> {
                        model.wavePhase = readCh.toDouble()
                        mode = ParseMode.NONE
                    }
                    ParseMode.M_PHASE -> {
                        currentMuscle.phase = readCh.toDouble()
                        mode = ParseMode.NONE
                    }
                    ParseMode.W_SPEED -> {
                        model.waveSpeed = readCh.toDouble()
                        mode = ParseMode.NONE
                    }
                    ParseMode.X -> {
                        currentMass.setX(readCh.toDouble())
                        mode = ParseMode.NONE
                    }
                    ParseMode.W_DIRECTION -> {
                        mode = ParseMode.NONE
                    }
                    ParseMode.SPRINGYNESS -> {
                        model.springyness = readCh.toDouble()
                        mode = ParseMode.NONE
                    }
                    ParseMode.Y -> {
                        currentMass.setY(readCh.toDouble())
                        model.addMass(currentMass)
                        masses[currentMass.id] = currentMass
                        mode = ParseMode.NONE
                    }
                    ParseMode.REST_LENGTH -> {
                        if (!createMuscle) {
                            currentSpring.restLength = readCh.toDouble()
                            currentSpring.mass1 = masses[mass1]!!
                            currentSpring.mass2 = masses[mass2]!!
                            model.addSpring(currentSpring)
                        } else {
                            currentMuscle.restLength = readCh.toDouble()
                            currentMuscle.mass1 = masses[mass1]!!
                            currentMuscle.mass2 = masses[mass2]!!
                            model.addMuscle(currentMuscle)
                        }
                        mode = ParseMode.NONE
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
                        if (mode == ParseMode.NONE) {
                            when (aValue) {
                                "friction" -> mode = ParseMode.FRICTION
                                "gravity" -> mode = ParseMode.GRAVITY
                                "vx" -> mode = ParseMode.VX
                                "vy" -> mode = ParseMode.VY
                                "x" -> mode = ParseMode.X
                                "y" -> mode = ParseMode.Y
                                "a" -> mode = ParseMode.MASS1
                                "b" -> mode = ParseMode.MASS2
                                "direction" -> mode = ParseMode.W_DIRECTION
                                "amplitude" -> if (createMuscle && !createWave) {
                                    mode = ParseMode.M_AMPLITUDE
                                } else if (createWave && !createMuscle) {
                                    mode = ParseMode.W_AMPLITUDE
                                }
                                "phase" -> if (!createWave && createMuscle) {
                                    mode = ParseMode.M_PHASE
                                } else if (createWave && !createMuscle) {
                                    mode = ParseMode.W_PHASE
                                }
                                "speed" -> mode = ParseMode.W_SPEED
                                "restLength" -> mode = ParseMode.REST_LENGTH
                                "wave" -> {
                                    createWave = true
                                    createMuscle = false
                                }
                                "springyness" -> mode = ParseMode.SPRINGYNESS
                            }
                        } else if (mode == ParseMode.NODE) {
                            if (aValue == "y") {
                                mode = ParseMode.NONE
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
                        mode = ParseMode.NODE
                    }
                } else if (qName == "ref") {
                    if (aQname == "refid" && aValue.startsWith("Mass")) {
                        if (mode == ParseMode.MASS1) {
                            mass1 = aValue.substring(5).toInt()
                            mode = ParseMode.NONE
                        } else if (mode == ParseMode.MASS2) {
                            mass2 = aValue.substring(5).toInt()
                            mode = ParseMode.NONE
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
        mode = ParseMode.NONE
        model = Model()
        source = InputSource(FileInputStream(path))
        val reader = XMLReaderFactory.createXMLReader()
        reader.contentHandler = this
        reader.parse(source)
    }
}