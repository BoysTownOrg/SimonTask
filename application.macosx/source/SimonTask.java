import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.sound.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class SimonTask extends PApplet {

Button [] buttons = new Button[4]; //<>// //<>//
int playerToneTime = 100;
DoneButton doneButton;
int textsize = 64;
SimonToneGenerator simonTones;
float myAmp=0.0f;
int [] colorsstart = {0xff00ff00, 0xffff0000, 0xffffff00, 0xff0000ff}, colors = new int[4]; 
int bgcolor = 255; //black = 0, 128 gray, 255 white; 
IntList colorscrambler = new IntList();
int index;
int instructNum = 1;
int [] simonSentence = new int[32];
int [] simonSentenceSave = new int[32];
int positionInSentence = 0;
int currentLengthOfTheSentence = 2;
int wrongCount = 0;
int clickCount = 0;

int lengthPresented ;
int practiceCount = 0;

int talkTime = 700;
int sequence = 0;

int hideTime = 700;
int nextTurnTime = 0;
int timeOut = 0;
int circSize = 300;
boolean isRandom;
int runNum = 0;
int trialCount = 0;
int maxRun = 30;
String[] colorAssign = {"", "", "", ""};

String [] circleInstructions;
String [] doPractice;
String [] goAhead;
String circleInstructionsText, doPracticeText, goAheadText;

boolean isSimonsTurn = true, instruct = true, notPractice = false;
boolean lastClick = true, simonStart=true;
int [] runtype = new int[maxRun];
int [] runlength = new int[maxRun];
Table table;
int rowCount = 0;
public void setup() {
  
  background(bgcolor);
  textSize(textsize);
  textAlign(CENTER);
  String[] lines = loadStrings("parameters.txt");
  circleInstructions = loadStrings("CircleInstructions.txt");
  doPractice = loadStrings("DoPractice.txt");
  goAhead = loadStrings("GoAhead.txt");
  circleInstructionsText = join(circleInstructions, "\n");
  doPracticeText = join(doPractice, "\n");
  goAheadText = join(goAhead, "\n");

  String[] list;
  for (int i = 0; i < lines.length; i++) {
    list = split(lines[i], " ");
    runtype[i] = PApplet.parseInt(list[0]);
    runlength[i] = PApplet.parseInt(list[1]);
    //println(str(runtype[i])+" "+str(runlength[i]));
  }
  maxRun = lines.length;
  println(maxRun);
  textSize(40);
  isRandom = runtype[runNum]==1;
  strokeWeight(4);
  colorscrambler.append(new int[] {0, 1, 2, 3});
  colorscrambler.shuffle();
  for (int i = 0; i < colorsstart.length; i++) {
    index = colorscrambler.get(i);
    colors[i] = colorsstart[index];
    colorAssign[i] = hex(colors[i], 6);
  }

  doneButton = new DoneButton(1, width*6/8, height*6/8, width/8, height/8);
  buttons[0] = new Button(0, width/2, height/4, circSize, colors[0]);
  buttons[1] = new Button(1, width/4, height/2, circSize, colors[1]);
  buttons[2] = new Button(2, width*3/4, height/2, circSize, colors[2]);
  buttons[3] = new Button(3, width/2, height*3/4, circSize, colors[3]);

  table = new Table();
  table.addColumn("Block");
  table.addColumn("RowCount");
  table.addColumn("position");
  table.addColumn("lengthPresented");
  table.addColumn("circleNum");
  table.addColumn("correct");
  table.addColumn("source");
  table.addColumn("isRandom");
  table.addColumn("TrialCount");
  table.addColumn("time");

  textAlign(CENTER, CENTER);


  simonTones = new SimonToneGenerator(this);

  textSize(40);
  textAlign(CENTER, CENTER);

  makeNewSentence(0); // these two statments make the fixed sequence
  arrayCopy(simonSentence, simonSentenceSave);
  //println(join(nf(simonSentenceSave, 0), ", "));
  makeNewSentence(1);

  //simonStartsNewGame(isRandom);
}

public void draw() {
  background(bgcolor);
  if (instruct) {
    textSize(textsize/2);
    fill(0);
    if (instructNum ==1)  text(circleInstructionsText, width/16, height/8, width*7/8, height*3/4);
    if (instructNum ==2)  text(doPracticeText, width/16, height/8, width*7/8, height*3/4);
    if (instructNum ==3)  text(goAheadText, width/16, height/8, width*7/8, height*3/4);
    textSize(textsize);
  } else {
    simonTones.checkPlayTime();
    if (simonTones.isPlayingTone == false) setButtonLightsOff();
    if (isSimonsTurn) simonSays();
    int buttonCount = 0;
    for (Button currentButton : buttons) {
      if (isSimonsTurn) {
        doneButton.isDisplayed = false;
        currentButton.myDarkColor = color(255);
        currentButton.myColor = currentButton.myDefaultColor;
      } else {
        if (millis() > nextTurnTime) {
          doneButton.isDisplayed = true;
          currentButton.myDarkColor = currentButton.myDefaultColor;
          currentButton.myColor = color(0);
          buttonCount++;
          if (buttonCount> 3) {
            nextTurnTime = millis()+100000;
          }
        }
      }
      currentButton.display();
    }
    doneButton.display();
    fill(255);
  }
}

public void simonSays() {

  if (millis() >= timeOut) {
    int simonsWord = simonSentence[positionInSentence];
    if (notPractice) {
      if (simonStart) {
        sequence++;
        simonStart = false;
      }
      lengthPresented = positionInSentence + 1;
      table.setFloat(rowCount, "time", millis());
      table.setInt(rowCount, "Block", runNum - 1);
      table.setInt(rowCount, "RowCount", rowCount + 1);
      table.setInt(rowCount, "position", positionInSentence + 1);
      table.setInt(rowCount, "lengthPresented", lengthPresented);
      table.setInt(rowCount, "TrialCount", sequence);
      table.setInt(rowCount, "circleNum", simonsWord + 1);
      table.setInt(rowCount, "correct", 1);  //1 = Simon
      table.setInt(rowCount, "source", 1);  //1 = Simon
      table.setInt(rowCount, "isRandom", runtype[runNum]);
      rowCount++;
    }
    //println(str(simonsWord));
    simonTones.playTone(simonsWord, talkTime, myAmp);
    buttons[simonsWord].isLightOn = true;
    if (positionInSentence < currentLengthOfTheSentence) {
      positionInSentence++;
    } else {
      nextTurnTime = millis() + talkTime + hideTime;
      isSimonsTurn = false;
      positionInSentence = 0;
    }

    timeOut = millis() + talkTime + hideTime;
  }
}


public void mousePressed() {

  if (doneButton.isMouseOver() == true) {
    if (notPractice) {
      simonStart = true;
    } else {
      if ((wrongCount == 0) && (clickCount - 1 == currentLengthOfTheSentence)) {
        instruct = true;
        instructNum++;
        if (instructNum == 2) {
          makeNewSentence(2);
        }
      }
    }
    doneButton.isDisplayed = false;
    isSimonsTurn = true;
    timeOut = millis() + talkTime + hideTime;
  } else { //a target button is pressed
    if (isSimonsTurn == false) { //only check clicks during player's turn
      for (Button currentButton : buttons) {
        if (currentButton.isMouseOver() == true) {
          clickCount++;
          currentButton.isLightOn = true;
          if (notPractice) {
            table.setFloat(rowCount, "time", millis());
            table.setInt(rowCount, "Block", runNum - 1);
            table.setInt(rowCount, "RowCount", rowCount + 1);
            table.setInt(rowCount, "position", positionInSentence + 1);
            table.setInt(rowCount, "lengthPresented", lengthPresented);
            table.setInt(rowCount, "TrialCount", sequence);
            table.setInt(rowCount, "circleNum", currentButton.myId + 1);
            if ((simonSentence[positionInSentence] == currentButton.myId) && (positionInSentence <= lengthPresented))  {
              table.setInt(rowCount, "correct", 1);
            } else {
              table.setInt(rowCount, "correct", 0);
            }
            table.setInt(rowCount, "source", 0);  //0 = player
            table.setInt(rowCount, "isRandom", runtype[runNum]);
            rowCount++;
          }
          if (simonSentence[positionInSentence] != currentButton.myId) {//wrong
            simonTones.playTone(4, playerToneTime, myAmp);
            wrongCount++;
          } else {
            simonTones.playTone(currentButton.myId, playerToneTime, myAmp);
          }
        }
      }
    }
  }
}

public void mouseReleased() {
  //println("released!");
  if (doneButton.isMouseOver() == true) {
    if ((wrongCount == 0) && (clickCount - 1 == currentLengthOfTheSentence)) {
      if (notPractice) {
        currentLengthOfTheSentence++;
      }
    } else {
      if (notPractice) {
        currentLengthOfTheSentence--;
      }
      if (currentLengthOfTheSentence < 0)
        currentLengthOfTheSentence = 0;
    }
    trialCount++;
    if (trialCount==runlength[runNum]) {
      runNum++;
      if (runNum == 2) notPractice = true;
      println(runNum);
      if (runNum==maxRun) {
        exit();
      }
      isRandom = runtype[runNum]==1;
      trialCount = 0;
    }
    if (isRandom) {
      makeNewSentence(0);
    } else {
      if (notPractice) arrayCopy(simonSentenceSave, simonSentence);
    }
    wrongCount = 0;//each turn starts you over
    clickCount = 0;
    lastClick = true;
    println("LastClick");
    positionInSentence = 0;

    timeOut = millis() + talkTime + hideTime;
  } else {
    if (isSimonsTurn == false) {
      if (positionInSentence < currentLengthOfTheSentence) {
        positionInSentence++;
      } else {  //positionInSentence >= currentLengthOfTheSentence
        boolean gameOver = currentLengthOfTheSentence == simonSentence.length-1;
        if (gameOver) {
          println("user wins!!!"); 
          simonStartsNewGame(isRandom);
        }
      }
    }
  }
}

public void setButtonLightsOff() {

  for (Button currentButton : buttons) {
    currentButton.isLightOn = false;
  }
}

public void simonStartsNewGame(boolean isRandom) {
  if (isRandom) {
    makeNewSentence(0);
  } else {
    arrayCopy(simonSentenceSave, simonSentence);
  }
  wrongCount = 0;
  timeOut = millis() + 1000;
  isSimonsTurn = true;
}

public void makeNewSentence(int predefined) {
  for (int i = 0; i<simonSentence.length; i++) {
    simonSentence[i] = PApplet.parseInt(random(0, 4));
  }
  if (predefined==1) {
    simonSentence[0] = 0;
    simonSentence[1] = 2;
    simonSentence[2] = 2;
  };
  if (predefined==2) {
    simonSentence[0] = 1;
    simonSentence[1] = 3;
    simonSentence[2] = 1;
  }


  positionInSentence = 0;
  //println(join(nf(simonSentence, 0), ", "));
}

public void keyPressed() {
  if (key == ' ' ) {
    instruct = false;
    if (instructNum == 3) {
      instruct = false;
      notPractice = true;
      currentLengthOfTheSentence = 0;
      setButtonLightsOff();
      doneButton.isDisplayed = false;
      isSimonsTurn = true;
      timeOut = millis() + talkTime + hideTime;
    }
  }
}

public void exit() {
  //it's over, baby
  String monthS = String.valueOf(month());
  String dayS = String.valueOf(day());
  String hourS = String.valueOf(hour());
  String minuteS = String.valueOf(minute());
  String myfilename = "Simon"+"-"+monthS +"-"+dayS+"-"+hourS+"-"+minuteS+".csv";
  saveTable(table, myfilename, "csv");
  myfilename = "Colors"+"-"+monthS +"-"+dayS+"-"+hourS+"-"+minuteS+".csv";
  saveStrings(myfilename, colorAssign);
  //println("exiting");
  super.exit();
}
class Button {

  int myId;

  float myX;
  float myY;
  float mySize;

  int myColor;
  int myDarkColor;
  int myDefaultColor;
  boolean isLightOn = false, isDark=false, myFlag;

  Button(int tempId, float tempX, float tempY, float tempSize, int tempColor) {
    myId = tempId;
    myX = tempX;
    myY = tempY;
    mySize = tempSize;
    myColor = tempColor;

    myDefaultColor = myColor;
    myDarkColor = color(255); //lerpColor(0, myColor, 0.5);
  }

  public void display() {
    if (isLightOn) {
      fill(myColor);
    } else {
      fill(myDarkColor);
    }
    circle(myX, myY, mySize);
  }

  public boolean isMouseOver() {
    float distance = sqrt(sq(myX-mouseX)+sq(myY-mouseY));
    //println(str(distance));
    if (distance < mySize/2)
    {
      return true;
    } else {
      return false;
    }
  }
}
class DoneButton {

  int myId;

  float myX;
  float myY;
  float myWidth;
  float myHeight;


  boolean isDisplayed = false;

  DoneButton(int tempId, float tempX, float tempY, float tempWidth, float tempHeight) {
    myId = tempId;
    myX = tempX;
    myY = tempY;
    myWidth = tempWidth;
    myHeight = tempHeight;
  }

  public void display() {
    if (isDisplayed) {
      fill(255);
      rect(myX, myY, myWidth, myHeight);
      fill(0);
      text("Done", myX+myWidth/2, myY+myHeight/2);
      fill(255);
    }
  }

  public boolean isMouseOver() {

    if (mouseX > myX && mouseX < (myX + myWidth) && 
      mouseY > myY  && mouseY < (myY + myHeight)) {

      return true;
    } else {
      return false;
    }
  }
}


class SimonToneGenerator {

  // Green: G4, Red: E4, Yellow: C4, Blue: G3, Wrong: G1
  float [] simonTones = { 391.995f, 329.628f, 261.626f, 195.998f, 48.9994f };
  
  SqrOsc wave;

  int toneStopTime;
  
  boolean isPlayingTone = false;

  SimonToneGenerator(PApplet p) {
    wave = new SqrOsc(p);
    wave.play();
    wave.amp(0.0f);
  }
  
  public void playTone(int index, int toneDuration, float amp) {
    
    wave.amp(amp);  
    wave.freq(simonTones[index]);
    
    toneStopTime = millis() + toneDuration;
    isPlayingTone = true;
  }
  
  public void checkPlayTime() {
    
    if(isPlayingTone) {
      if(millis() >= toneStopTime) {
        stopTone();
      }
    }
  }
  
  public void stopTone() {
    
    if(isPlayingTone) {
      wave.amp(0.0f);
      isPlayingTone = false; 
    }
    
  }
  
}
/*

    4 colored buttons
      each button has a light
      each button has a unique sound. 
      
    Simon talks by playing a note and lights up a button.
    
    User talks back by pressing the button (light/sound). 
    
    Simon can check if the user gives the right answer. 
    
      if(yes) he goes on...
      if(no) he stops by playing a wrong note/light and restarts again

      The user can win...
    
    ====
    Interface
    
    Green – G4 391.995 Hz
    Red – E4 329.628 Hz
    Yellow – C4 261.626 Hz
    Blue – G3 195.998 Hz
    
    Wrong - G1 48.9994 Hz
    
    Sequence length: 1-5, tone duration 0.42 seconds, pause between tones 0.05 seconds
    Sequence length: 6-13, tone duration 0.32 seconds, pause between tones 0.05 seconds
    Sequence length: 14-31, tone duration 0.22 seconds, pause between tones 0.05 seconds
    
    Re-Dooh:
    Change one thing at a time.
    1. Use a done button to indicate your turn is over
      - two aspects: continue clicks. click to change turn
        -extend clicks
          - isWrong not enabled for wrong answer
          - remove isWrong; increment wrongCount for wrong response at given position
          - don't increment currentLengthOfTheSentence if wrongCount > 0; reset per turn
          - cosmetic - make tones the same length (give player tones and flash a constant length)
          ** note that each position is judged independently, regardly of previous mistakes on that turn **
          - make Simon default be background, player default be bright
     **status - wrongly changes immediately to Simon mode after last user click, but heading for done button with circular targets**
     basic sequence works
     goal: switch between random and constant
*/
  public void settings() {  fullScreen(); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "SimonTask" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
