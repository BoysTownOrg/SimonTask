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

public class SimonRe_Dooh extends PApplet {


Button [] buttons = new Button[4];

DoneButton doneButton;

SimonToneGenerator simonTones;
float myAmp=0.0f;
int [] colorsstart = {0xff00ff00, 0xffff0000, 0xffffff00, 0xff0000ff}, colors = new int[4]; 
int bgcolor = 255; //black = 0, 128 gray, 255 white; 
IntList colorscrambler = new IntList();
int index;

int [] simonSentence = new int[32];
int [] simonSentenceSave = new int[32];
int positionInSentence = 0;
int currentLengthOfTheSentence = 0;
int wrongCount = 0;
int clickCount = 0;
int talkTime = 700;
int playerToneTime = 100;
int hideTime = 700;
int nextTurnTime = 0;
int timeOut = 0;
int circSize = 300;
boolean isRandom;
int runNum = 0;
int trialCount = 0;
int maxRun = 4;
String[] colorAssign = {"", "", "", ""};

boolean isSimonsTurn = true;
boolean lastClick = true;
int [] runtype = new int[4];
int [] runlength = new int[4];
Table table;
int rowCount = 0;
public void setup() {
  
  background(bgcolor);
  String[] lines = loadStrings("parameters.txt");
  String[] list;
  for (int i = 0; i < lines.length; i++) {
    list = split(lines[i], " ");
    runtype[i] = PApplet.parseInt(list[0]);
    runlength[i] = PApplet.parseInt(list[1]);
    println(str(runtype[i])+" "+str(runlength[i]));
  }
  maxRun = lines.length;
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
  saveStrings("colors.txt", colorAssign);
  doneButton = new DoneButton(1, width*6/8, height*6/8, width/8, height/8);
  buttons[0] = new Button(0, width/2, height/4, circSize, colors[0]);
  buttons[1] = new Button(1, width/4, height/2, circSize, colors[1]);
  buttons[2] = new Button(2, width*3/4, height/2, circSize, colors[2]);
  buttons[3] = new Button(3, width/2, height*3/4, circSize, colors[3]);

  table = new Table();
  table.addColumn("runNum");
  table.addColumn("position");
  table.addColumn("circleNum");
  table.addColumn("source");
  table.addColumn("isRandom");
  table.addColumn("time");

  textAlign(CENTER, CENTER);


  simonTones = new SimonToneGenerator(this);

  textSize(40);
  textAlign(CENTER, CENTER);
  
  makeNewSentence();
  arrayCopy(simonSentence, simonSentenceSave);
    println(join(nf(simonSentenceSave, 0), ", "));

  simonStartsNewGame(isRandom);
}

public void draw() {
  background(bgcolor);
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

public void simonSays() {

  if (millis() >= timeOut) {

    int simonsWord = simonSentence[positionInSentence];
    table.setFloat(rowCount, "time", millis());
    table.setInt(rowCount, "runNum", runNum);
    table.setInt(rowCount, "position", positionInSentence);
    table.setInt(rowCount, "circleNum", simonsWord);
    table.setInt(rowCount, "source", 1);  //1 = Simon
    table.setInt(rowCount, "isRandom", runtype[runNum]);
    rowCount++;
    println(str(simonsWord));
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
    doneButton.isDisplayed = false;
    isSimonsTurn = true;
    timeOut = millis() + talkTime + hideTime;
  } else { //a target button is pressed
    if (isSimonsTurn == false) { //only check clicks during player's turn
      clickCount++;
      for (Button currentButton : buttons) {
        if (currentButton.isMouseOver() == true) {
          currentButton.isLightOn = true;
          table.setFloat(rowCount, "time", millis());
          table.setInt(rowCount, "runNum", runNum);
          table.setInt(rowCount, "position", positionInSentence);
          table.setInt(rowCount, "circleNum", currentButton.myId);
          table.setInt(rowCount, "source", 0);  //0 = player
          table.setInt(rowCount, "isRandom", runtype[runNum]);
          rowCount++;

          println(str(currentButton.myId));
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
      currentLengthOfTheSentence++;
    } else {
      currentLengthOfTheSentence--;
      if (currentLengthOfTheSentence < 0)
        currentLengthOfTheSentence = 0;
    }
    trialCount++;
    if (trialCount==runlength[runNum]) {
      runNum++;
      if (runNum==maxRun) {
        exit();
      }
      isRandom = runtype[runNum]==1;
      trialCount = 0;
    }
    if (isRandom) {
      makeNewSentence();
    } else {
      arrayCopy(simonSentenceSave, simonSentence);
    }
    wrongCount = 0;//each trun starts you over
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
    makeNewSentence();
  } else {
    arrayCopy(simonSentenceSave, simonSentence);
  }
  wrongCount = 0;
  timeOut = millis() + 1000;
  isSimonsTurn = true;
}

public void makeNewSentence() {
  for (int i = 0; i<simonSentence.length; i++) {
    simonSentence[i] = PApplet.parseInt(random(0, 4));
  }

  positionInSentence = 0;
  println(join(nf(simonSentence, 0), ", "));
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
    String[] appletArgs = new String[] { "SimonRe_Dooh" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
