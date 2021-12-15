package com;

import java.io.Serializable;
/**
 * Класс позволяющий «дать жизнь» объекту так же между запусками программы.
 */
public class Command implements Serializable {
    CommandType type;
    public CommandType getType(){
        return type;
    }
}
