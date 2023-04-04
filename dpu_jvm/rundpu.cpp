#include <dpu>
#include <iostream>
using namespace dpu;



int main(void) {

    try {
        auto system = DpuSet::allocate(1);
        auto dpu = system.dpus()[0];
        
        dpu->load("dpuslave");
        dpu->exec();

        dpu->log(std::cout);
    }
    catch (const DpuError& e) {
        std::cerr << e.what() << std::endl;
    }
    return 0;
}