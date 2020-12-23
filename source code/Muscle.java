public class Muscle extends Spring{
  private double amplitude,phase;
  
  public Muscle(int name){
    super.name=name;
  }//Muscle
  
  public void setAmplitude(double a){
    amplitude=a;
  }// setAmplitude
  
  public void setPhase(double p){
    phase=p;
  }//setPhase
  
  public double getAmplitude(){
    return amplitude;
  }//getAmplitude
  
  public double getPhase(){
    return phase;
  }//getPhase
  
  public String toString(){
    return name+" a:"+mass1+" b:"+mass2+" amp:"+amplitude+" phase:"+phase+" restLeng:"+restLength;
  }//toString
}//class