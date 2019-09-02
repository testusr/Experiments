package smeo.experiments.quickfixj;


import quickfix.*;

public class MsgParser {
    public static void main(String[] args) {
        String msg="8=FIX.4.29=11835=A34=149=Client.CompID50=ClientSubId52=20190705-02:24:54.37756=Server.CompID57=Server.SubId98=0108=30141=Y10=206";
        String msg2="35=A34=149=Client.CompID50=ClientSubId52=20190705-02:24:54.37756=Server.CompID57=Server.SubId98=0108=30141=Y";

        try {
            System.out.println((MessageUtils.parse(new DefaultMessageFactory(), null, msg2).toRawString()))
            ;
        } catch (InvalidMessage invalidMessage) {

            invalidMessage.printStackTrace();
        }
    }
}
