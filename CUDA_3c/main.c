#include <stdio.h>
#include <stdlib.h>
#include <time.h>

#include "alloy.h"
//#include "gpu.h"
#include "cpu.h"
#include "main.h"

int main(int argc, char** argv) {
    srand(time(NULL));

    int width = 256;
    int height = 256;

    //materials_def mat_def = create_materials_def(0.75, 1.0, 1.25);
    materials_def mat_def = create_materials_def(1.50, 1.0, 0.50);
    //materials_def mat_def = create_materials_def(1.0, 1.0, 1.0);
    alloy* my_alloy = create_alloy(width, height, mat_def);

    stamp_dots(my_alloy);
    stamp_pattern(my_alloy);

    int num_generations = 1500;
    for (int i = 0; i < num_generations; i++) {
        update_alloy(i, my_alloy);

        char *path = (char*)malloc((7 + 5 + 4 + 1) * sizeof(char));
        sprintf(path, "images/%05d.png", i);

        write_alloy_png(my_alloy, path);

        free(path);

        printf("%d\n", i);
    }

    free_alloy(my_alloy);

    return 0;
}
