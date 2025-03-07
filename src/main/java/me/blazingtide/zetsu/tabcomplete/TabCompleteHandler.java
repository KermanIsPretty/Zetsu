package me.blazingtide.zetsu.tabcomplete;

import com.google.common.collect.Maps;
import lombok.Getter;
import me.blazingtide.zetsu.Zetsu;
import me.blazingtide.zetsu.schema.CachedCommand;
import me.blazingtide.zetsu.tabcomplete.listener.TabCompleteListener;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TabCompleteHandler {

    private final Map<String, List<String>> subcommandCache = Maps.newHashMap(); //Instead of looping every time, we store the sub commands and loop ONCE. (under the impression sub commands won't be created mid runtime)

    private final Zetsu zetsu;

    @Getter
    private final TabCompleteListener listener;

    public TabCompleteHandler(Zetsu zetsu) {
        this.zetsu = zetsu;

        this.listener = new TabCompleteListener(zetsu, this, zetsu.getProcessor());
    }

    @Deprecated
    public Set<String> request(String command) {
        throw new UnsupportedOperationException("Unfinished");
    }

    @Nullable
    public List<String> requestSubcommands(@Nullable String label) {
        if (label == null) {
            return null;
        }
        /*
        very slow because if we have ~100 commands registered, it's going to do a loop every tab complete = lags the server...
        even tho it's barely noticeable, it will add up with a lot of players

        return zetsu.getLabelMap().getOrDefault(label, Lists.newArrayList()).stream().map(new Function<CachedCommand, String>() {
            @Override
            public String apply(CachedCommand cachedCommand) {
                return cachedCommand.getLabel().replace(label, "");
            }
        });
         */

        return subcommandCache.computeIfAbsent(label, string -> {
            String[] split = string.split(Zetsu.CMD_SPLITTER);

            List<CachedCommand> topLevel = zetsu.getLabelMap().get(string.contains(Zetsu.CMD_SPLITTER) ? split[0].trim() : string.trim());

            if (topLevel == null || topLevel.isEmpty()) {
                return null;
            }

            List<String> list = new ArrayList<>();

            for (CachedCommand command : topLevel) {
                if (command.getArgs().isEmpty() || (string.endsWith(Zetsu.CMD_SPLITTER) ? split.length : split.length - 1) > command.getArgs().size()) {
                    continue;
                }

                // We need this if statement for /tabablecommand tab1 tab2 because /tabablecommand tab3 will show tab2 for the next one
                if ((command.getLabel() + Zetsu.CMD_SPLITTER + StringUtils.join(command.getArgs(), Zetsu.CMD_SPLITTER)).startsWith(string)) {
                    list.add(command.getArgs().get((string.endsWith(Zetsu.CMD_SPLITTER) ? split.length : split.length - 1) - 1));
                }
            }

            return list;
        });
    }

}
