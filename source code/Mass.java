//import java.awt.geom.Point2D;

public class Mass{
  private final double WEIGHT=1.0;
  private int name;
  private double vx=0,vy=0,x=0,y=0,fx=0,fy=0,oldVx=0,oldVy=0,oldX=0,oldY=0;
  private boolean toRevertX=false, toRevertY=false;
  public Mass(int name){
    this.name=name;
  }//mass constructor
  
  public double getX(){
    return x;
  } //getX
  public double getY(){
    return y;
  } //getY

  public double getVx(){
    return vx;
  }//getVx
  public double getVy(){
    return vy;
  }//getVy
  public void setX(double x){
    oldX=this.x;
    this.x=x;
  }//setX
  public void setY(double y){
    oldY=this.y;
    this.y=y;
  }//setX
  public void setVx(double vx){
    oldVx=this.vx;
    this.vx=vx;
  }//setX
  public void setVy(double vy){
    oldVy=this.vy;
    this.vy=vy;
  }//setX
  public double getOldVx(){
  	return oldVx;
  }//getOldVx
  public double getOldVy(){
  	return oldVy;
  }//getOldVy
  public double getOldX(){
  	return oldX;
  }//getOldX
  public double getOldY(){
  	return oldY;
  }//getOldY
  public void revertX(){
  	x=oldX;
  }//revertX
  public void revertY(){
  	y=oldY;
  }//revertY
  public int getName(){
    return name;
  }//getName
  public void finallyRevert(){
    if(toRevertX){
      x=oldX;
      toRevertX=false;}
    if(toRevertY){
      y=oldY;
      toRevertY=false;}
  }//
  /*public int getIndex(){//Mass#100
  	return Integer.parseInt(name.substring(5,name.length()));
  }*/
  public String toString(){
    return name+" Vx:"+vx+" Vy:"+vy+" X:"+x+" Y:"+y;
  }//to String
} // Mass