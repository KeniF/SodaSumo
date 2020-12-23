import java.util.HashMap;
import java.util.Collection;

public class Model {
//for performance boosting: data structure should be <int,Mass>
private HashMap<Integer,Mass> massMap=new HashMap<Integer,Mass>();
private HashMap<Integer,Spring> springMap=new HashMap<Integer,Spring>();
private HashMap<Integer,Muscle> muscleMap=new HashMap<Integer,Muscle>();
private double friction,gravity,springyness,wavePhase,waveSpeed,waveAmplitude,
                     boundTop=-10000,boundBottom=10000,boundRight=-10000,boundLeft=10000;
private int waveDirection;
private String name;
  public Model(){
  }//Model
  
  public double getGravity(){
    return gravity;
  }//getGravity
  
  public double getFriction(){
    return friction;
  }//getFriction
  
  public double getSpringyness(){
    return springyness;
  }//getSpringyness
  
  public double getWavePhase(){
    return wavePhase;
  }//getWavePhase
  
  public double getWaveSpeed(){
    return waveSpeed;
  }//getWaveSpeed
  
  public double getWaveAmplitude(){
    return waveAmplitude;
  }//getWaveAmplitude
  
  public void setFriction(double f){
    friction=f;
  }//setFriction
  
  public void setName(String n){
    name=n;
  }//setName
  
  public String getName(){
    return name;
  }//getName
  
  public void setGravity(double g){
    gravity=g;
  }//setGravity
  
  public void setSpringyness(double s){
    springyness=s;
  }//setSpringyness
  
  public void setWaveAmplitude(double a){
    waveAmplitude=a;
  }//setWaveAmplitude

  public void setWaveDirection(int d){
    waveDirection=d;
  }//setWaveDirection
    
  public void setWavePhase(double p){
    wavePhase=p;
  }//setWavePhase
  
  public void setWaveSpeed(double s){
    waveSpeed=s;
  }//setWaveSpeed
  
  public void addMass(Mass m){
    massMap.put(m.getName(),m); //adds mass to massMap
    adjustBoundRect(m);
  }//addMass
  
  public void adjustBoundRect(Mass m){
    if(m.getX()<boundLeft)
      boundLeft=m.getX();
    if(m.getX()>boundRight)
      boundRight=m.getX();
    if(m.getY()>boundTop)
      boundTop=m.getY();
    if(m.getY()<boundBottom)
      boundBottom=m.getY();
  }//adjustBoundTri
  
  public void resetBoundRect(){
  	boundTop=-10000;boundBottom=10000;boundRight=-10000;boundLeft=10000;
  }//resetBoundRect
  
  public double[] getBoundingRectangle(){
    double boundingRectangle[]=new double[4];
    boundingRectangle[0]=boundTop;
    boundingRectangle[1]=boundBottom;
    boundingRectangle[2]=boundLeft;
    boundingRectangle[3]=boundRight;
    return boundingRectangle;
  }//getBoundRectangle
  
  public double getBoundRight(){
  	return boundRight;
  }//getBoundRight
  
  public double getBoundLeft(){
  	return boundLeft;
  }//getBoundLeft
  
  public Mass getMass(int s){
    return massMap.get(s);
  }//getMass
  
  public Spring getSpring(int s){
    return springMap.get(s);
  }//getSpring
  
  public Muscle getMuscle(int s){
    return muscleMap.get(s);
  }//getMuscle
  
  public Mass[] getAllMass(){
    Collection<Mass> collection=massMap.values();
    return (Mass[])collection.toArray(new Mass[0]);
  }//getAllMass
  
  public Spring[] getAllSpring(){
    Collection<Spring> collection=springMap.values();
    return (Spring[])collection.toArray(new Spring[0]);
  }//getAllSpring
  
  public Muscle[] getAllMuscle(){
    Collection<Muscle> collection=muscleMap.values();
    return (Muscle[])collection.toArray(new Muscle[0]);
  }//getAllMuscle
  
  public void addSpring(Spring s){
    springMap.put(s.getName(),s); //adds spring to massMap
  }//addSpring
  
  public void addMuscle(Muscle m){
    muscleMap.put(m.getName(),m);//adds muscle
  }//addMuscle
  
  public int totalNoOfMass(){
    return massMap.size();
  }//totalNoOfMass
  
  public int totalNoOfSpring(){
    return springMap.size();
  }//totalNoOfMass
  
  public int totalNoOfMuscle(){
    return muscleMap.size();
  }//totalNoOfMuscle
  
  public String toString(){
    return "[Model] "+"Masses:"+massMap.size()+"\nSprings:"+springMap.size()+"\nMuscles:"
      +muscleMap.size();
  }//toString
}//Model