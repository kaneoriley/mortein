package kaneoriley.mortein.internal;

import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import kaneoriley.mortein.DebugBool;
import kaneoriley.mortein.DebugDouble;
import kaneoriley.mortein.DebugFloat;
import kaneoriley.mortein.DebugInt;
import kaneoriley.mortein.DebugLong;
import kaneoriley.mortein.DebugString;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
@Accessors(prefix = "s")
@Aspect
public final class Mortein {

    private static final String TAG = Mortein.class.getSimpleName();

    @Nullable
    private static final Logger log;

    private static volatile long sGlobalStartNanos;

    @Getter
    @Setter
    private static volatile boolean sLoggingEnabled = true;

    static {
        Logger logger;
        try {
            logger = LoggerFactory.getLogger(Mortein.class);
        } catch (NoClassDefFoundError e) {
            logger = null;
        }
        log = logger;
        sGlobalStartNanos = System.nanoTime();
    }

    /*
     *  Logging
     */

    @Pointcut("within(@kaneoriley.mortein.DebugLog(enabled=true) *)")
    public void withinAnnotatedClass() {
    }

    @Pointcut("execution(* *(..)) && withinAnnotatedClass()")
    public void methodInsideAnnotatedType() {
    }

    @Pointcut("execution(*.new(..)) && withinAnnotatedClass()")
    public void constructorInsideAnnotatedType() {
    }

    @Pointcut("execution(@kaneoriley.mortein.DebugLog(enabled=true) * *(..)) || methodInsideAnnotatedType()")
    public void method() {
    }

    @Pointcut("execution(@kaneoriley.mortein.DebugLog(enabled=true) *.new(..)) || constructorInsideAnnotatedType()")
    public void constructor() {
    }

    @Around("method() || constructor()")
    @Nullable
    public Object logAndExecute(@NonNull ProceedingJoinPoint joinPoint) throws Throwable {
        enterMethod(joinPoint);

        long startNanos = System.nanoTime();
        Object result = joinPoint.proceed();
        long stopNanos = System.nanoTime();
        long lengthMillis = TimeUnit.NANOSECONDS.toMillis(stopNanos - startNanos);
        long globalLengthMillis = TimeUnit.NANOSECONDS.toMillis(stopNanos - sGlobalStartNanos);

        exitMethod(joinPoint, result, lengthMillis, globalLengthMillis);

        return result;
    }

    private static void enterMethod(@NonNull JoinPoint joinPoint) {
        if (!sLoggingEnabled) {
            return;
        }
        CodeSignature codeSignature = (CodeSignature) joinPoint.getSignature();

        Class<?> cls = codeSignature.getDeclaringType();
        String methodName = codeSignature.getName();
        String[] parameterNames = codeSignature.getParameterNames();
        Object[] parameterValues = joinPoint.getArgs();

        StringBuilder builder = new StringBuilder(asTag(cls))
                .append(" \u21E2 ")
                .append(methodName)
                .append('(');

        for (int i = 0; i < parameterValues.length; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(parameterNames[i]).append('=');
            builder.append(Strings.toString(parameterValues[i]));
        }
        builder.append(')');

        if (Looper.myLooper() != Looper.getMainLooper()) {
            builder.append(" [Thread:\"").append(Thread.currentThread().getName()).append("\"]");
        }

        if (log != null) {
            log.debug(builder.toString());
        } else {
            Log.d(TAG, builder.toString());
        }
    }

    private static void exitMethod(@NonNull JoinPoint joinPoint,
                                   @Nullable Object result,
                                   long lengthMillis,
                                   long globalLengthMillis) {
        if (!sLoggingEnabled) {
            return;
        }
        Signature signature = joinPoint.getSignature();

        Class<?> cls = signature.getDeclaringType();
        String methodName = signature.getName();
        boolean hasReturnType = signature instanceof MethodSignature
                && ((MethodSignature) signature).getReturnType() != void.class;

        StringBuilder builder = new StringBuilder(asTag(cls))
                .append(" \u21E0 ")
                .append(methodName)
                .append(" [")
                .append(lengthMillis)
                .append("ms (")
                .append(globalLengthMillis)
                .append("ms)]");

        if (hasReturnType) {
            builder.append(" = ");
            builder.append(Strings.toString(result));
        }

        if (log != null) {
            log.debug(builder.toString());
        } else {
            Log.d(TAG, builder.toString());
        }
    }

    @NonNull
    private static String asTag(@NonNull Class<?> cls) {
        if (cls.isAnonymousClass()) {
            return asTag(cls.getEnclosingClass());
        }
        return cls.getSimpleName();
    }

    /*
     *  Boolean
     */

    @Pointcut("get(@kaneoriley.mortein.DebugBool(enabled=true) * *.*)")
    public void boolField() {
    }

    @Pointcut("execution(@kaneoriley.mortein.DebugBool(enabled=true) * *(..))")
    public void boolMethod() {
    }

    @Pointcut("boolField() || boolMethod()")
    public void boolFieldOrMethod() {
    }

    @Around("boolFieldOrMethod() && @annotation(debugBool)")
    public boolean replaceValue(@NonNull ProceedingJoinPoint joinPoint, @NonNull DebugBool debugBool) {
        return debugBool.value();
    }

    /*
     *  Double
     */

    @Pointcut("get(@kaneoriley.mortein.DebugDouble(enabled=true) * *.*)")
    public void doubleField() {
    }

    @Pointcut("execution(@kaneoriley.mortein.DebugDouble(enabled=true) * *(..))")
    public void doubleMethod() {
    }

    @Pointcut("doubleField() || doubleMethod()")
    public void doubleFieldOrMethod() {
    }

    @Around("doubleFieldOrMethod() && @annotation(debugDouble)")
    public double replaceValue(@NonNull ProceedingJoinPoint joinPoint, @NonNull DebugDouble debugDouble) {
        return debugDouble.value();
    }

    /*
     *  Float
     */

    @Pointcut("get(@kaneoriley.mortein.DebugFloat(enabled=true) * *.*)")
    public void floatField() {
    }

    @Pointcut("execution(@kaneoriley.mortein.DebugFloat(enabled=true) * *(..))")
    public void floatMethod() {
    }

    @Pointcut("floatField() || floatMethod()")
    public void floatFieldOrMethod() {
    }

    @Around("floatFieldOrMethod() && @annotation(debugFloat)")
    public float replaceValue(@NonNull ProceedingJoinPoint joinPoint, @NonNull DebugFloat debugFloat) {
        return debugFloat.value();
    }

    /*
     *  Integer
     */

    @Pointcut("get(@kaneoriley.mortein.DebugInt(enabled=true) * *.*)")
    public void intField() {
    }

    @Pointcut("execution(@kaneoriley.mortein.DebugInt(enabled=true) * *(..))")
    public void intMethod() {
    }

    @Pointcut("intField() || intMethod()")
    public void intFieldOrMethod() {
    }

    @Around("intFieldOrMethod() && @annotation(debugInt)")
    public int replaceValue(@NonNull ProceedingJoinPoint joinPoint, @NonNull DebugInt debugInt) {
        return debugInt.value();
    }

    /*
     *  Long
     */

    @Pointcut("get(@kaneoriley.mortein.DebugLong(enabled=true) * *.*)")
    public void longField() {
    }

    @Pointcut("execution(@kaneoriley.mortein.DebugLong(enabled=true) * *(..))")
    public void longMethod() {
    }

    @Pointcut("longField() || longMethod()")
    public void longFieldOrMethod() {
    }

    @Around("longFieldOrMethod() && @annotation(debugLong)")
    public long replaceValue(@NonNull ProceedingJoinPoint joinPoint, @NonNull DebugLong debugLong) {
        return debugLong.value();
    }

    /*
     *  String
     */

    @Pointcut("get(@kaneoriley.mortein.DebugString(enabled=true) * *.*)")
    public void stringField() {
    }

    @Pointcut("execution(@kaneoriley.mortein.DebugString(enabled=true) * *(..))")
    public void stringMethod() {
    }

    @Pointcut("stringField() || stringMethod()")
    public void stringFieldOrMethod() {
    }

    @Around("stringFieldOrMethod() && @annotation(debugString)")
    public String replaceValue(@NonNull ProceedingJoinPoint joinPoint, @NonNull DebugString debugString) {
        return debugString.value();
    }

}
