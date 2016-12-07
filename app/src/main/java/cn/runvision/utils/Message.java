package cn.runvision.utils;

public class Message {
    public final static int SIZE = 8;
   
    private String strName;
    private String pwd;
    public   int nameSize = 0;

    public Message() {
        
    }

    public Message(byte code, byte light, byte color, byte speed) {
       
    }
    public Message(String name, String pwd) {
        this.strName = name;
        this.pwd = pwd;
        
        this.nameSize = name.length()+pwd.length();
       
    }
    public byte[] identiyToByteArray() {
        byte[] bytes = new byte[nameSize+1];
        String str = strName +" " + pwd;
        bytes = str.getBytes(); 
        printBytes(bytes);
        return bytes;
    }
    
    public void printBytes(byte[] bytes) {
        StringBuilder strb = new StringBuilder();
        for (byte bt: bytes) {
            strb.append(bt & 0xFF);
            strb.append(" ");
        }
        System.out.println(strb);
    }

}
