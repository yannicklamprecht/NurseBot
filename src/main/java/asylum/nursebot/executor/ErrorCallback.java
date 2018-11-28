package asylum.nursebot.executor;

import java.util.function.BiConsumer;

public interface ErrorCallback extends BiConsumer<Exception, CallbackContext>{

}
