class Button {

  int myId;

  float myX;
  float myY;
  float mySize;

  color myColor;
  color myDarkColor;
  color myDefaultColor;
  boolean isLightOn = false, isDark=false, myFlag;

  Button(int tempId, float tempX, float tempY, float tempSize, color tempColor) {
    myId = tempId;
    myX = tempX;
    myY = tempY;
    mySize = tempSize;
    myColor = tempColor;

    myDefaultColor = myColor;
    myDarkColor = color(255); //lerpColor(0, myColor, 0.5);
  }

  void display() {
    if (isLightOn) {
      fill(myColor);
    } else {
      fill(myDarkColor);
    }
    circle(myX, myY, mySize);
  }

  boolean isMouseOver() {
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
