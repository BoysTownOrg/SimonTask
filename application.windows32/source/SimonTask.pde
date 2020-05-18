
Button [] buttons = new Button[4];

DoneButton doneButton;

SimonToneGenerator simonTones;
float myAmp=0.0;
color [] colorsstart = {#00ff00, #ff0000, #ffff00, #0000ff}, colors = new color[4]; 
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
void setup() {
  fullScreen();
  background(bgcolor);
  String[] lines = loadStrings("parameters.txt");
  String[] list;
  for (int i = 0; i < lines.length; i++) {
    list = split(lines[i], " ");
    runtype[i] = int(list[0]);
    runlength[i] = int(list[1]);
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

void draw() {
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

void simonSays() {

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


void mousePressed() {

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

void mouseReleased() {
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

void setButtonLightsOff() {

  for (Button currentButton : buttons) {
    currentButton.isLightOn = false;
  }
}

void simonStartsNewGame(boolean isRandom) {
  if (isRandom) {
    makeNewSentence();
  } else {
    arrayCopy(simonSentenceSave, simonSentence);
  }
  wrongCount = 0;
  timeOut = millis() + 1000;
  isSimonsTurn = true;
}

void makeNewSentence() {
  for (int i = 0; i<simonSentence.length; i++) {
    simonSentence[i] = int(random(0, 4));
  }

  positionInSentence = 0;
  println(join(nf(simonSentence, 0), ", "));
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
