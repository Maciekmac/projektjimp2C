#include <stdio.h>
#include <stdlib.h>
#include "config.h"
#include "parser.h" 

int main(int argc, char *argv[]) {
    Config config;

    int parse_status = parse_arguments(argc, argv, &config);
    if (parse_status == 0) {
        return SUCCESS;
    } else if (parse_status != 1) {
        return parse_status;
    }

    FILE *in_file = fopen(config.input_file, "r");
    if (in_file == NULL) {
        fprintf(stderr, "Blad: Nie mozna otworzyc pliku wejsciowego: %s\n", config.input_file);
        return ERR_INPUT_READ;
    }

    //wczytywanie danych
    Graph my_graph;
    init_graph(&my_graph); //inicjalizacja pamieci

    int load_status = load_graph(in_file, &my_graph); // Czytanie plik
    fclose(in_file); 

    if (load_status != SUCCESS) {
        free_graph(&my_graph); 
        return load_status;    
    }

    printf("Sukces! Wczytano %d krawedzi z pliku.\n", my_graph.count);

    
    free_graph(&my_graph);

    return SUCCESS;
}
