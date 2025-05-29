package dev.ftb.mods.ftblibrary.util;


import com.google.common.collect.ImmutableList;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public record SearchTerms(List<Term> terms) {
    public static SearchTerms parse(String str) {
        if (str.isEmpty()) return new SearchTerms(List.of());

        List<Term> terms = new ArrayList<>();

        for (String s : str.toLowerCase().split(" +")) {
            terms.add(Term.of(s));
        }

        return new SearchTerms(ImmutableList.copyOf(terms));
    }

    public boolean match(ResourceLocation id, String displayName, Predicate<ResourceLocation> tagMatcher) {
        return terms.stream().allMatch(term -> {
           if (term.value.isEmpty()) return true;
           return switch (term.type) {
               case MOD -> id.getNamespace().contains(term.value);
               case TAG -> tagMatcher.test(id);
               case SIMPLE -> displayName.toLowerCase().contains(term.value);
           };
        });
    }

    public record Term(TermType type, String value) {
        public static Term of(String s) {
            if (s.isEmpty()) return new Term(TermType.SIMPLE, "");
            return switch (s.charAt(0)) {
                case '@' -> new Term(TermType.MOD, s.substring(1));
                case '#' -> new Term(TermType.TAG, s.substring(1));
                default -> new Term(TermType.SIMPLE, s);
            };
        }
    }

    private enum TermType {
        SIMPLE,
        MOD,
        TAG
    }
}
