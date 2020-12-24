package com.ihd2

import java.io.FilenameFilter
import java.io.IOException
import java.lang.NullPointerException
import org.xml.sax.SAXException
import java.awt.Dimension
import kotlin.jvm.JvmStatic
import java.awt.Color
import java.awt.BorderLayout
import java.awt.event.*
import java.io.File
import java.lang.Exception
import javax.swing.*

class Sodasumo private constructor() : JFrame(), MouseListener, ItemListener, ActionListener, KeyListener {
    private val loadButton: JButton
    private val stopButton: JButton
    private val invertM1Button: JCheckBox
    private val invertM2Button: JCheckBox
    private val newGameDraw = GameDraw()
    private val aboutItemSoda: JMenuItem
    private val aboutItemAuthor: JMenuItem
    private var xmlFiles: Array<String> = arrayOf()
    private var invertM1 = false
    private var invertM2 = false
    private val box1: JComboBox<Any>
    private val box2: JComboBox<Any>
    private val boxTime: JComboBox<String>
    private var xp1: XmlParser? = null
    private var xp2: XmlParser? = null
    private fun loadDirectory() {
        try {
            val currentDir = System.getProperty("user.dir")
            val dir = File(currentDir)
            val filter = FilenameFilter { _, name: String -> name.endsWith(".xml") }
            xmlFiles = dir.list(filter)
            if (xmlFiles.isEmpty()) throw Exception()
            for (i in xmlFiles.indices) {
                xmlFiles[i] = xmlFiles[i].substring(0, xmlFiles[i].length - 4)
            }
        } catch (e: Exception) {
            JOptionPane.showMessageDialog(
                this, "No XML files in directory!!\n",
                "Error", JOptionPane.WARNING_MESSAGE
            )
        }
    }

    override fun itemStateChanged(e: ItemEvent) {
        val source = e.itemSelectable
        if (source === invertM1Button) {
            invertM1 = !invertM1
            newGameDraw.invertM1()
        } else if (source === invertM2Button) {
            invertM2 = !invertM2
            newGameDraw.invertM2()
        }
    }

    override fun actionPerformed(e: ActionEvent) {
        when {
            e.source === box1 -> {
                box1.showPopup()
            }
            e.source === box2 -> {
                box2.showPopup()
            }
            e.source === boxTime -> {
                newGameDraw.setTimeLimit((boxTime.selectedItem?.toString()?.toDouble() ?: 0.0) * 1000.0)
            }
        }
    }

    override fun mouseExited(e: MouseEvent) {}
    override fun mouseClicked(e: MouseEvent) {}
    override fun mouseEntered(e: MouseEvent) {}
    override fun keyTyped(e: KeyEvent) {}
    override fun keyPressed(e: KeyEvent) {
        try {
            if ((e.keyCode == KeyEvent.VK_ENTER ||
                        e.keyCode == KeyEvent.VK_SPACE) && loadButton.isVisible
            ) {
                val xmlFile1 = box1.selectedItem?.toString() ?: ""
                xp1 = XmlParser("$xmlFile1.xml")
                if (xp1!!.verified != 2) throw Exception("Model 1 cannot be verified!")
                val xmlFile2 = box2.selectedItem?.toString() ?: ""
                xp2 = XmlParser("$xmlFile2.xml")
                if (xp2!!.verified != 2) throw Exception("Model 2 cannot be verified!")
                val model1 = xp1!!.model
                model1.name = box1.selectedItem?.toString() ?: ""
                val model2 = xp2!!.model
                model2.name = box2.selectedItem?.toString() ?: ""
                newGameDraw.provideModel1(model1)
                newGameDraw.provideModel2(model2)
                loadButton.isVisible = false
                stopButton.isVisible = true
                stopButton.grabFocus()
                box1.isEnabled = false
                box2.isEnabled = false
                newGameDraw.startDraw()
                newGameDraw.init()
            } else if ((e.keyCode == KeyEvent.VK_ENTER ||
                        e.keyCode == KeyEvent.VK_SPACE) && stopButton.isVisible
            ) {
                newGameDraw.pause()
                stopButton.isVisible = false
                loadButton.isVisible = true
                box1.isEnabled = true
                loadButton.grabFocus()
                box2.isEnabled = true
            }
        } catch (f: IOException) {
            JOptionPane.showMessageDialog(
                newGameDraw, "Please ensure the XML files are in the same directory.\n$f",
                "Error During Load", JOptionPane.WARNING_MESSAGE
            )
        } catch (s: SAXException) {
            JOptionPane.showMessageDialog(
                newGameDraw, "Invalid XML files.\n$s",
                "Error During Load", JOptionPane.WARNING_MESSAGE
            )
        } catch (ed: NullPointerException) {
            JOptionPane.showMessageDialog(
                newGameDraw, "Model contains objects not permitted in SodaSumo\n$ed",
                "Error During Load", JOptionPane.WARNING_MESSAGE
            )
        } catch (ds: Exception) {
            JOptionPane.showMessageDialog(
                newGameDraw, "Error when loading models\n$ds",
                "Error During Load", JOptionPane.WARNING_MESSAGE
            )
        }
    }

    override fun keyReleased(e: KeyEvent) {}
    override fun mousePressed(e: MouseEvent) {
        try {
            val xmlFile1: String
            val xmlFile2: String
            when {
                e.source === loadButton -> {
                    xmlFile1 = box1.selectedItem?.toString() ?: ""
                    xp1 = XmlParser("$xmlFile1.xml")
                    if (xp1!!.verified != 2) throw Exception("Model 1 cannot be verified!")
                    xmlFile2 = box2.selectedItem?.toString() ?: ""
                    xp2 = XmlParser("$xmlFile2.xml")
                    if (xp2!!.verified != 2) throw Exception("Model 2 cannot be verified!")
                    val model1 = xp1!!.model
                    model1.name = box1.selectedItem?.toString() ?: ""
                    val model2 = xp2!!.model
                    model2.name = box2.selectedItem?.toString() ?: ""
                    newGameDraw.provideModel1(model1)
                    newGameDraw.provideModel2(model2)
                    loadButton.isVisible = false
                    stopButton.isVisible = true
                    box1.isEnabled = false
                    box2.isEnabled = false
                    newGameDraw.startDraw()
                    newGameDraw.init()
                }
                e.source === stopButton -> {
                    newGameDraw.pause()
                    loadButton.isVisible = true
                    stopButton.isVisible = false
                    box1.isEnabled = true
                    box2.setEnabled(true)
                }
                e.source === aboutItemSoda -> {
                    JOptionPane.showMessageDialog(
                        newGameDraw,
                        """
                                <html><font size=5 color=blue><b><u>$VERSION</u></b></font></html>
                                
                                SodaSumo is a clone/extension to the physics game SodaRace.
                                While SodaRace allows users to import several models designed
                                in SodaConstructor for a competition in moving speed,
                                SodaSumo replicates the SodaConstructor physics engine
                                and takes the game further by implementing collision detection
                                between two models.
                                
                                More information at
                                <html><a href="http://sodaplay.com">www.sodaplay.com</a></html>
                                """.trimIndent(),
                        "About", JOptionPane.INFORMATION_MESSAGE
                    )
                }
                e.source === aboutItemAuthor -> {
                    JOptionPane.showMessageDialog(
                        newGameDraw,
                        """<html><font size=5 color=blue><b><u>Who made SodaSumo?</u></b></font></html>
        
        (C) 2008, 2020 Kenneth "KeniF" Lam | kenif.lam@gmail.com
        
        This program was started as my third year project
        at the University of Manchester, United Kingdom.
        Supervised by Dr. Mary McGee Wood""",
                        "About", JOptionPane.INFORMATION_MESSAGE
                    )
                }
            }
        } catch (f: IOException) {
            JOptionPane.showMessageDialog(
                newGameDraw, "Please ensure the XML files are in the same directory\n$f",
                "Error During Load", JOptionPane.WARNING_MESSAGE
            )
        } catch (s: SAXException) {
            JOptionPane.showMessageDialog(
                newGameDraw, "Invalid XML files.\n$s",
                "Error During Load", JOptionPane.WARNING_MESSAGE
            )
        } catch (es: NullPointerException) {
            JOptionPane.showMessageDialog(
                newGameDraw, "Model contains objects not permitted in SodaSumo\n$es",
                "Error During Load", JOptionPane.WARNING_MESSAGE
            )
        } catch (ds: Exception) {
            JOptionPane.showMessageDialog(
                newGameDraw, "Error when loading models\n$ds",
                "Error During Load", JOptionPane.WARNING_MESSAGE
            )
        }
    }

    override fun mouseReleased(e: MouseEvent) {}

    companion object {
        private const val VERSION = "SodaSumo 1.6"
        private val GAME_DIMENSION = Dimension(1000, 350)
        private val CP_DIMENSION = Dimension(150, 320)
        var GAME_WIDTH = GAME_DIMENSION.getWidth() - CP_DIMENSION.getWidth()
        @JvmStatic
        fun main(args: Array<String>) {
            val newGame = Sodasumo()
            newGame.isVisible = true
        }
    }

    init {
        loadDirectory()
        val menuBar = JMenuBar()
        this.jMenuBar = menuBar
        menuBar.background = Color.GRAY.brighter().brighter()
        title = VERSION
        val aboutMenu = JMenu("About")
        menuBar.add(aboutMenu)
        aboutItemSoda = JMenuItem(VERSION)
        aboutMenu.add(aboutItemSoda)
        aboutItemSoda.addMouseListener(this)
        aboutItemAuthor = JMenuItem("Author")
        aboutMenu.add(aboutItemAuthor)
        aboutItemAuthor.addMouseListener(this)
        val contents = contentPane
        contents.layout = BorderLayout()
        contents.background = Color.WHITE
        size = GAME_DIMENSION
        isResizable = false
        contents.add(newGameDraw)
        val cPanel = JPanel()
        contents.add(cPanel, BorderLayout.EAST)
        cPanel.background = Color.GRAY.brighter()
        cPanel.preferredSize = CP_DIMENSION
        box1 = JComboBox(xmlFiles)
        box1.maximumRowCount = 10
        box1.prototypeDisplayValue = "THIS IS A VERY LON"
        box1.selectedItem = "daintywalker"
        box1.isEditable = true
        box1.addActionListener(this)
        cPanel.add(box1)
        invertM1Button = JCheckBox("Invert Direction", false)
        invertM1Button.background = Color.GRAY.brighter()
        invertM1Button.addItemListener(this)
        cPanel.add(invertM1Button)
        box2 = JComboBox(xmlFiles)
        box2.maximumRowCount = 10
        box2.prototypeDisplayValue = "THIS IS A VERY LON"
        box2.selectedItem = "KeniF_triangle"
        box2.isEditable = true
        box2.addActionListener(this)
        cPanel.add(box2)
        invertM2Button = JCheckBox("Invert Direction", false)
        invertM2Button.background = Color.GRAY.brighter()
        invertM2Button.addItemListener(this)
        cPanel.add(invertM2Button)
        loadButton = JButton("  Start  ")
        loadButton.addMouseListener(this)
        loadButton.addKeyListener(this)
        loadButton.addActionListener(this)
        cPanel.add(loadButton)
        stopButton = JButton("  Stop  ")
        stopButton.addMouseListener(this)
        stopButton.addKeyListener(this)
        stopButton.isVisible = false
        stopButton.addActionListener(this)
        cPanel.add(stopButton)
        val emptySpace = JLabel("                                    ")
        val emptySpace2 = JLabel("                                    ")
        val eS3 = JLabel("                                    ")
        val label = JLabel("    Time Limit")
        cPanel.add(emptySpace)
        cPanel.add(emptySpace2)
        cPanel.add(eS3)
        cPanel.add(label)
        val times = arrayOf("5", "10", "15", "20", "25", "30", "60")
        boxTime = JComboBox(times)
        boxTime.maximumRowCount = 10
        boxTime.selectedItem = "15"
        boxTime.isEditable = false
        boxTime.addActionListener(this)
        boxTime.toolTipText = "Set Time Limit"
        boxTime.prototypeDisplayValue = "30"
        cPanel.add(boxTime)
        defaultCloseOperation = EXIT_ON_CLOSE
        setLocationRelativeTo(null)
    }
}