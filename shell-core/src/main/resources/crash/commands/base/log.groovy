import ch.qos.logback.classic.LoggerContext
import org.crsh.cli.*
import org.crsh.cli.completers.EnumCompleter
import org.crsh.cli.descriptor.ParameterDescriptor
import org.crsh.cli.spi.Completer
import org.crsh.cli.spi.Completion
import org.crsh.command.InvocationContext
import org.crsh.command.Pipe
import org.crsh.text.Color
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.util.regex.Pattern

@Usage("Log commands")
class log {

    static Collection<Logger> getLoggers() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory()

        return context.getLoggerList();
    }

    @Usage("send a message to a sl4j logger")
    @Man("""\
The send command log one or several loggers with a specified message. For instance the
following impersonates the javax.management.mbeanserver class and send a message on its own
logger.

#% log send -m hello javax.management.mbeanserver

Send is a <Logger, Void> command, it can log messages to consumed log objects:

% log ls | log send -m hello -l warn""")
    @Command
    Pipe<Logger, Object> send(@MsgOpt String msg, @LoggerArg String name, @LevelOpt Level level) {
        level = level ?: Level.info
        return new Pipe<Logger, Object>() {
            @Override
            void open() {
                if (name != null) {
                    Logger logger = LoggerFactory.getLogger(name);
                    level.log(logger, msg)
                }
            }

            @Override
            void provide(Logger logger) {
                level.log(logger, msg)
            }
        }
    }

    @Usage("list the available loggers")
    @Man("""\
The log ls command list all the available loggers, for instance:

% log ls
org.apache.catalina.core.ContainerBase.[Catalina].[localhost].[/].[default]
org.apache.catalina.core.ContainerBase.[Catalina].[localhost].[/eXoGadgetServer].[concat]
org.apache.catalina.core.ContainerBase.[Catalina].[localhost].[/dashboard].[jsp]
...

The -f switch provides filtering with a Java regular expression

% log ls -f javax.*
javax.management.mbeanserver
javax.management.modelmbean

The log ls command is a <Void,Logger> command, therefore any logger produced can be
consumed.""")
    @Command
    void ls(InvocationContext<Logger> context, @FilterOpt String filter) {
        // Regex filter
        def pattern = Pattern.compile(filter ?: ".*")

        loggers.each {
            def matcher = it =~ pattern;
            if (matcher.matches()) {
                context.provide(it)
            }
        }
    }

    @Usage("create one or several loggers")
    @Command
    void add(InvocationContext<Logger> context, @LoggerArg List<String> names) {
        names.each {
            if (it.length() > 0) {
                Logger logger = LoggerFactory.getLogger(it);
                if (logger != null) {
                    context.provide(logger);
                }
            }
        }
    }

    @Man("""\
The set command sets the level of a logger. One or several logger names can be specified as
arguments and the -l option specify the level among the finest, finer, fine, info, warn and
severe levels. When no level is specified, the level is cleared and the level will be
inherited from its ancestors.

% log set -l trace foo
% log set foo

The logger name can be omitted and instead stream of logger can be consumed as it is a
<Logger,Void> command. The following set the level warn on all the available loggers:

% log ls | log set -l warn""")
    @Usage("configures the level of one of several loggers")
    @Command
    Pipe<Logger, Object> set(@LoggerArg List<String> names, @LevelOpt @Required Level level) {

        return new Pipe<Logger, Object>() {
            @Override
            void open() {
                names.each() {
                    def logger = LoggerFactory.getLogger(it);
                    level.setLevel(logger)
                }
            }

            @Override
            void provide(Logger logger) {
                level.setLevel(logger);
            }
        };
    }

    @Man("""\
The tail command provides a tail view of a list of loggers. One or several logger names can
be specified as argument and the -l option configures the level threshold. When no logger
name is specified, the root logger will be tailed, when no level is specified, the info
level will be used:

% log tail
Feb 10, 2014 1:50:36 PM java_util_logging_Logger\$log call
INFO: HELLO

The tail process will end upon interruption (ctrl-c).""")
    @Usage("tail loggers")
    @Command
    void tail(
            @Usage("the level treshold")
            @LevelOpt Level level,
            @Usage("the logger names to tail or empty for the root logger")
            @LoggerArg List<String> names
//            InvocationContext<LogRecord> context
    ) {
//        if (level == null) {
//            level = Level.info;
//        }
//        def loggers = []
//        if (names != null && names.size() > 0) {
//            names.each { loggers << Logger.getLogger(it) }
//        } else {
//            loggers = [Logger.getLogger("")]
//        }
//        def handler = new StreamHandler() {
//            @Override
//            synchronized void publish(LogRecord record) {
//                if (record.level.intValue() >= level.value.intValue()) {
//                    context.provide(record);
//                    context.flush();
//                }
//            }
//
//            @Override
//            synchronized void flush() {
//                context.flush();
//            }
//
//            @Override
//            void close() throws SecurityException {
//                // ?
//            }
//        };
//        loggers.each { it.addHandler(handler); }
//        def lock = new Object();
//        try {
//            synchronized (lock) {
//                // Wait until ctrl-c
//                lock.wait();
//            }
//        } finally {
//            loggers.each { it.removeHandler(handler); }
//        }
    }
}

enum Level {
    trace(Color.blue, { Logger logger, String msg -> logger.trace(msg) }),
    debug(Color.blue, { Logger logger, String msg -> logger.debug(msg) }),
    info(Color.white, { Logger logger, String msg -> logger.info(msg) }),
    warn(Color.yellow, { Logger logger, String msg -> logger.warn(msg) }),
    error(Color.red, { Logger logger, String msg -> logger.error(msg) });

    final Color color
    final Closure closure

    Level(Color color, Closure<?> closure) {
        this.color = color
        this.closure = closure
    }

    void log(Logger logger, String msg) {
        closure.call(logger, msg)
    }

    static void setLevel(Logger logger) {
        ch.qos.logback.classic.Logger l = (ch.qos.logback.classic.Logger) logger
        l.setLevel(ch.qos.logback.classic.Level.WARN)
    }
}

class LoggerCompleter implements Completer {

    static Collection<Logger> getLoggers() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory()

        return context.getLoggerList();
    }

    @Override
    Completion complete(ParameterDescriptor parameter, String prefix) throws Exception {
        def builder = new Completion.Builder(prefix)
        loggers.each() {
            if (it.name.startsWith(prefix)) {
                builder.add(it.name.substring(prefix.length()), true)
            }
        }
        return builder.build();
    }
}

@Retention(RetentionPolicy.RUNTIME)
@Usage("the logger level")
@Man("The logger level to assign among {trace, debug, info, warn, error}")
@Option(names = ["l", "level"], completer = EnumCompleter)
@interface LevelOpt {}

@Retention(RetentionPolicy.RUNTIME)
@Usage("the message")
@Man("The message to log")
@Option(names = ["m", "message"])
@Required
@interface MsgOpt {}

@Retention(RetentionPolicy.RUNTIME)
@Usage("the logger name")
@Man("The name of the logger")
@Argument(name = "name", completer = LoggerCompleter.class)
@Required
@interface LoggerArg {}

@Retention(RetentionPolicy.RUNTIME)
@Usage("a regexp filter")
@Man("A regular expressions used to filter the loggers")
@Option(names = ["f", "filter"])
@interface FilterOpt {}
