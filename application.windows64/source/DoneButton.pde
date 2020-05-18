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

  void display() {
    if (isDisplayed) {
      fill(255);
      rect(myX, myY, myWidth, myHeight);
      fill(0);
      text("Done", myX+myWidth/2, myY+myHeight/2);
      fill(255);
    }
  }

  boolean isMouseOver() {

    if (mouseX > myX && mouseX < (myX + myWidth) && 
      mouseY > myY  && mouseY < (myY + myHeight)) {

      return true;
    } else {
      return false;
    }
  }
}
