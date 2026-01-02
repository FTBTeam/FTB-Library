package dev.ftb.mods.ftblibrary.util;

import net.minecraft.resources.Identifier;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public record SearchTerms(List<Term> terms) {
    public static SearchTerms parse(String str) {
        if (str.isEmpty()) return new SearchTerms(List.of());

        List<Term> terms = Arrays.stream(str.toLowerCase().split(" +"))
                .map(Term::of)
                .toList();

        return new SearchTerms(terms);
    }

    public boolean match(Identifier id, String displayName, Predicate<Identifier> tagMatcher) {
        return terms.stream().allMatch(term -> {
           if (term.value.isEmpty()) return true;
           return switch (term.type) {
               case MOD -> id.getNamespace().contains(term.value);
               case TAG -> tagMatcher.test(Identifier.parse(term.value));
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
