package su.nexmedia.engine.actions.parameter;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.utils.regex.RegexUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

public abstract class AbstractParametized {

    protected static final NexEngine ENGINE = NexEngine.get();
    protected static final String                       FLAG_NO_DELAY = "@NODELAY@";
    private static final   Map<String, ParameterResult> RESULT_CACHE  = new HashMap<>();
    protected final String                    name;
    protected final Set<AbstractParameter<?>> parameters;

    public AbstractParametized(@NotNull String name) {
        this.name = name.toLowerCase();
        this.parameters = new HashSet<>();
    }

    public static void clearCache() {
        RESULT_CACHE.clear();
    }

    @NotNull
    public final String getName() {
        return this.name;
    }

    protected final void registerParameter(@NotNull String id) {
        AbstractParameter<?> parameter = ENGINE.getActionsManager().getParameter(id);
        if (parameter == null) {
            ENGINE.error("Trying to register an invalid param '" + id + "' !");
            return;
        }
        this.parameters.add(parameter);
    }

    @NotNull
    public final Set<AbstractParameter<?>> getParameters() {
        return this.parameters;
    }

    @NotNull
    public final ParameterResult getParameterResult(@NotNull String str) {
        String cache = str.replace(FLAG_NO_DELAY, "");
        if (RESULT_CACHE.containsKey(cache)) return RESULT_CACHE.get(cache);

        Map<String, Object> values = new HashMap<>();
        for (AbstractParameter<?> parameter : this.getParameters()) {
            String flag = parameter.getFlag(); // Raw flag, without '~' prefix
            if (!str.contains(flag)) continue;

            // Search for flag of this parameter
            Matcher matcher = RegexUtil.getMatcher(parameter.getPattern(), str);
            if (matcher == null) {
                ENGINE.warn("Could not process regex matcher for parameter values!");
                continue;
            }

            // Get the flag value
            if (matcher.find()) {
                String valueRaw = matcher.group(4).trim(); // Extract only value from all flag string
                Object valueParsed = parameter.getParser().parse(valueRaw);
                values.put(parameter.getName(), valueParsed);
            }
        }

        ParameterResult result = new ParameterResult(values);
        RESULT_CACHE.put(cache, result);
        return result;
    }
}