package com.alura.literalura.dto;
import com.fasterxml.jackson.annotation.JsonAlias;

public record AuthorDTO(@JsonAlias("name")
                        String authorName,

                        @JsonAlias("birth_year")
                        int birthYear,

                        @JsonAlias("death_year")
                        int deathYear
) {
    @Override
    public String toString() {
        return "----- Autor -----" +
                "\n Nombre: " + authorName +
                "\n Fecha de Nacimiento: " + birthYear +
                "\n Fecha de Fallecimiento: " + deathYear +
                "\n ---------------\n";
    }
}
