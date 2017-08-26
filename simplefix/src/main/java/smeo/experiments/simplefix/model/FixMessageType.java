package smeo.experiments.simplefix.model;

public enum FixMessageType {
    Logon("A"),
    Unknown("");

    private final String msgType;

    FixMessageType(String msgType) {
        this.msgType = msgType;
    }

    CharSequence msgType() {
        return msgType;
    }

    public static FixMessageType lookup(CharSequence charSequence) {
        for (int i = 0; i < values().length; i++) {
            final FixMessageType currType = values()[i];
            if (currType != Unknown) {
                if (equals(currType.msgType(), charSequence)) {
                    return currType;
                }
            }
        }
        return Unknown;
    }

    private static boolean equals(CharSequence charSequence, CharSequence charSequence1) {
        if (charSequence.length() == charSequence1.length()) {
            for (int i = 0; i < charSequence.length(); i++) {
                if (charSequence.charAt(i) != charSequence1.charAt(i)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
