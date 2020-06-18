Button [] buttons = new Button[4]; //<>//
int playerToneTime = 500;
DoneButton doneButton;
int textsize = 64;
SimonToneGenerator simonTones;
float myAmp=0.0;
color [] colorsstart = {#00ff00, #ff0000, #ffff00, #0000ff}, colors = new color[4]; 
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
int practiceCount = 0;
int numRight=0,numTotal=0;
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
boolean lastClick = true,simonStart=true;
int [] runtype = new int[maxRun];
int [] runlength = new int[maxRun];
Table table;
int rowCount = 0;
void setup() {
  fullScreen();
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
    runtype[i] = int(list[0]);
    runlength[i] = int(list[1]);
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
  table.addColumn("circleNum");
  table.addColumn("source");
  table.addColumn("isRandom");
  table.addColumn("TrialCount");
  table.addColumn("numRight");
  table.addColumn("numTotal");
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

void draw() {
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

void simonSays() {

  if (millis() >= timeOut) {
    int simonsWord = simonSentence[positionInSentence];
    if (notPractice) {
      if (simonStart) {
        sequence++;
        simonStart = false;
      }
      table.setFloat(rowCount, "time", millis());
      table.setInt(rowCount, "Block", runNum - 1);
      table.setInt(rowCount, "RowCount", rowCount);
      table.setInt(rowCount, "position", positionInSentence);
      table.setInt(rowCount, "TrialCount", sequence);
      table.setInt(rowCount, "circleNum", simonsWord + 1);
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


void mousePressed() {

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
            table.setInt(rowCount, "RowCount", rowCount);
            table.setInt(rowCount, "position", positionInSentence);
            table.setInt(rowCount, "TrialCount", sequence);
            table.setInt(rowCount, "circleNum", currentButton.myId + 1);
            table.setInt(rowCount, "source", 0);  //0 = player
            table.setInt(rowCount, "numRight", numRight);  //0 = player
            table.setInt(rowCount, "numTotal", numTotal);  //0 = player
            table.setInt(rowCount, "isRandom", runtype[runNum]);
            rowCount++;
          }
          if (simonSentence[positionInSentence] != currentButton.myId) {//wrong
            simonTones.playTone(4, playerToneTime, myAmp);
            wrongCount++;
          } else {
            numRight += 1;
            simonTones.playTone(currentButton.myId, playerToneTime, myAmp);
          }
        }
      }
    }
  }
}

void mouseReleased() {
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
      isRandom = runtype[runNum]==1; //<>// //<>//
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
      numTotal += 1;
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

void setButtonLightsOff() {

  for (Button currentButton : buttons) {
    currentButton.isLightOn = false;
  }
}

void simonStartsNewGame(boolean isRandom) {
  if (isRandom) {
    makeNewSentence(0);
  } else {
    arrayCopy(simonSentenceSave, simonSentence);
  }
  wrongCount = 0;
  timeOut = millis() + 1000;
  isSimonsTurn = true;
}

void makeNewSentence(int predefined) {
  for (int i = 0; i<simonSentence.length; i++) {
    simonSentence[i] = int(random(0, 4));
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

void keyPressed() {
  if (key == ' ' ) {
    instruct = false;
    if (instructNum == 3) {
      instruct = false;
      notPractice = true;
      currentLengthOfTheSentence = 0;
      setButtonLightsOff();
      doneButton.isDisplayed = false;
      isSimonsTurn = true; //<>// //<>//
      numRight=0;
      numTotal=0;
      timeOut = millis() + talkTime + hideTime;
    }
  }
}

void exit() {
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
