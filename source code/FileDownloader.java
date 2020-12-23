import java.io.*;
import java.net.*;

//takes in username and modelname
//attempts to download modelname.xml file
public class FileDownloader{
  public FileDownloader(){
  }//
  public File download(String unsplit, File file){
    OutputStream out=null;
    URLConnection conn=null;
    InputStream  in=null;
    String[]name=unsplit.split("/");
    try{
      URL url=new URL("http://sodaplay.com/creators/"+name[0]+"/items/"+name[1]+".xml");
      //System.out.println(url);
      out=new BufferedOutputStream(new FileOutputStream(file));
      conn=url.openConnection();
      in=conn.getInputStream();
      byte[] buffer = new byte[1024];
      int numRead;
      long numWritten=0;
      while ((numRead= in.read(buffer)) != -1) {
        out.write(buffer, 0, numRead);
        //numWritten += numRead;
      }//while
    }//try
    catch (Exception exception) {
      file=null;
    } finally {
    try {
      if (in != null) in.close();
      if (out != null) out.close();
    }//try
    catch (IOException ioe) {file=null;}
    }//finally
    return file;
  }//download
}//class