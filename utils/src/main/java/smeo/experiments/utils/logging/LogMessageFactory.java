package smeo.experiments.utils.logging;

import org.apache.log4j.Logger;

/**
 * Simple wrapper to beautify the creation of log messages a bit
 */
public class LogMessageFactory {
    private final Logger logger;

    public LogMessageFactory(Class<?> clazz){
        this.logger = Logger.getLogger(clazz);
    }
    public LogMessageFactory(Logger logger) {
        this.logger = logger;
    }

    public LogMessage message(){
        return new LogMessage(logger);
    }


    public static class LogMessage {
        private final Logger logger;
        private final long creationTimeMs;
        private final long creationTimeNs;
        private StringBuilder messageBuilder = new StringBuilder();

        public LogMessage(Logger logger) {
            this.logger = logger;
            this.creationTimeMs = System.currentTimeMillis();
            this.creationTimeNs = System.nanoTime();
        }

        public LogMessage append(Object obj) {
            messageBuilder.append(obj);
            return this;
        }

        public LogMessage append(String str) {
            messageBuilder.append(str);
            return this;
        }

        public LogMessage append(StringBuffer sb) {
            messageBuilder.append(sb);
            return this;
        }

        public LogMessage append(CharSequence s) {
            messageBuilder.append(s);
            return this;
        }

        public LogMessage append(CharSequence s, int start, int end) {
            messageBuilder.append(s, start, end);
            return this;
        }

        public LogMessage append(char[] str) {
            messageBuilder.append(str);
            return this;
        }

        public LogMessage append(char[] str, int offset, int len) {
            messageBuilder.append(str, offset, len);
            return this;
        }

        public LogMessage append(boolean b) {
            messageBuilder.append(b);
            return this;
        }

        public LogMessage append(char c) {
            messageBuilder.append(c);
            return this;
        }

        public LogMessage append(int i) {
            messageBuilder.append(i);
            return this;
        }

        public LogMessage append(long lng) {
            messageBuilder.append(lng);
            return this;
        }

        public LogMessage append(float f) {
            messageBuilder.append(f);
            return this;
        }

        public LogMessage append(double d) {
            messageBuilder.append(d);
            return this;
        }

        public LogMessage appendAsXml(Object object){
            messageBuilder.append("#### XML-START ####\n");
            messageBuilder.append(XmlUtils.objectToXml(object));
            messageBuilder.append("#### XML-END ####\n");
            return this;
        }

        public LogMessage appendLn(String message){
            messageBuilder.append(message);
            newline();
            return this;
        }

        public LogMessage newline(){
            messageBuilder.append("\n");
            return this;
        }

        public LogMessage logInfo(){
            logger.info(messageBuilder.toString());
            return this;
        }

        public LogMessage logDebug(){
            logger.info(messageBuilder.toString());
            return this;
        }

        public LogMessage logError(){
            logger.info(messageBuilder.toString());
            return this;
        }

        public LogMessage logStdout(){
            System.out.println(messageBuilder.toString());
            return this;
        }

        public String toString(){
            return messageBuilder.toString();
        }


    }
}
