
Button [] buttons = new Button[4];

DoneButton doneButton;

SimonToneGenerator simonTones;
float myAmp=0.6;
color [] colorsstart = {#00ff00, #ff0000, #ffff00, #0000ff}, colors = new color[4]; 
int bgcolor = 255; //black = 0, 128 gray, 255 white; 
IntList colorscrambler = new IntList();
int index;

int [] simonSentence = new int[32];
int positionInSentence = 0;
int currentLengthOfTheSentence = 1;
int wrongCount = 0;
int clickCount = 0;
int talkTime = 420;
int playerToneTime = 420;
int nextTurnTime = 0;
int timeOut = 0;
int circSize = 300;

boolean isSimonsTurn = true;
boolean lastClick = true;

void setup() {
  fullScreen();
  background(bgcolor);

  textSize(40);

  strokeWeight(4);
  colorscrambler.append(new int[] {0, 1, 2, 3});
  colorscrambler.shuffle();
  for (int i = 0; i < colorsstart.length; i++) {
    index = colorscrambler.get(i);
    colors[i] = colorsstart[index];
  }
  doneButton = new DoneButton(1, width*6/8, height*6/8, width/8, height/8);
  buttons[0] = new Button(0, width/2, height/4, circSize, colors[0]);
  buttons[1] = new Button(1, width/4, height/2, circSize, colors[1]);
  buttons[2] = new Button(2, width*3/4, height/2, circSize, colors[2]);
  buttons[3] = new Button(3, width/2, height*3/4, circSize, colors[3]);


  textAlign(CENTER, CENTER);


  simonTones = new SimonToneGenerator(this);

  textSize(40);
  textAlign(CENTER, CENTER);

  simonStartsNewGame();
}

void draw() {

  simonTones.checkPlayTime();

  if (simonTones.isPlayingTone == false) setButtonLightsOff();

  if (isSimonsTurn) simonSays();
  int buttonCount = 0;
  for (Button currentButton : buttons) {
    if (isSimonsTurn) {
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

  //if (isSimonsTurn) {
  //  if (currentLengthOfTheSentence == 0) text("Simon Starts", width/2, height/2); 
  //  else                                text("Simons Turn", width/2, height/2);
  //} else {
  //  text("Your Turn", width/2, height/2);
  //}
}

void simonSays() {

  if (millis() >= timeOut) {

    int simonsWord = simonSentence[positionInSentence];
    simonTones.playTone(simonsWord, talkTime, myAmp);
    buttons[simonsWord].isLightOn = true;
    if (positionInSentence < currentLengthOfTheSentence) {
      positionInSentence++;
    } else {
      nextTurnTime = millis() + talkTime + 1000;
      isSimonsTurn = false;
      positionInSentence = 0;
    }

    timeOut = millis() + talkTime + 1000;
  }
}


void mousePressed() {

  if (doneButton.isMouseOver() == true) {
    doneButton.isDisplayed = false;
    isSimonsTurn = true;
    timeOut = millis() + talkTime + 1000;
  } else { //a target button is pressed
    if (isSimonsTurn == false) { //only check clicks during player's turn
      clickCount++;
      for (Button currentButton : buttons) {
        if (currentButton.isMouseOver() == true) {

          currentButton.isLightOn = true;

          if (simonSentence[positionInSentence] != currentButton.myId) {//wrong
            simonTones.playTone(4, playerToneTime, myAmp);
            wrongCount++;
            println("Inc wrongCount");
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
    println(str(clickCount)+ " " + str(currentLengthOfTheSentence));
    if ((wrongCount == 0) && (clickCount - 1 == currentLengthOfTheSentence)) {
      currentLengthOfTheSentence++;
      println("inc currentLengthOfTheSentence");
    }
    wrongCount = 0;//each trun starts you over
    clickCount = 0;
    lastClick = true;
    println("LastClick");
    positionInSentence = 0;

    timeOut = millis() + talkTime + 1000;
  } else {
    if (isSimonsTurn == false) {

      if (positionInSentence < currentLengthOfTheSentence) {
        positionInSentence++;
      } else {  //positionInSentence >= currentLengthOfTheSentence
        boolean gameOver = currentLengthOfTheSentence == simonSentence.length-1;
        if (gameOver) {
          println("user wins!!!"); 
          simonStartsNewGame();
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

void simonStartsNewGame() {

  makeNewSentence();
  wrongCount = 0;
  timeOut = millis() + 1000;
  isSimonsTurn = true;
}

void makeNewSentence() {
  for (int i = 0; i<simonSentence.length; i++) {
    simonSentence[i] = int(random(0, 4));
  }

  positionInSentence = 0;
  currentLengthOfTheSentence = 1;

  println(join(nf(simonSentence, 0), ", "));
}
