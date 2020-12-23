public class Spring {
  protected int name,mass1_name,mass2_name;
  protected Mass mass1,mass2;
  protected double restLength;
  
  public Spring(int name) {
    this.name=name;
  }//Spring constructor
  
  public Spring(){
  }// empty constructor, needed for muscle
  
  public int getName(){
    return name;
  }//getName
  
  public Mass getMass1(){
    return mass1;
  }//getMass1
  
  public Mass getMass2(){
    return mass2;
  }//getMass2
  
  public double getRestLength(){
    return restLength;
  }//getRestLength
  
  public void setMass1(Mass m){
    mass1=m;
  }//setMass1
  
  public void setMass2(Mass m){
    mass2=m;
  }//setMass2
  
  public void setRestLength(double r){
    this.restLength=r;
  }//setRestLength
  
  public String toString(){
    return name+" a:"+mass1+" b:"+mass2+" restlength:"+restLength;
  }//toString

}//Spring