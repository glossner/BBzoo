from abc import ABC

class StorageArchitecture(ABC):
    """
    Base class for all computer architectures in the zoo.
    Defines the fundamental parameters of the design space.
    """
    word_width: int
    address_width: int

class StackArchitecture(StorageArchitecture):
    """
    Stack Architecture (zero-address storage hierarchy).
    Operands are implicitly on top of a stack.
    """
    stack_depth: int

class AccumulatorArchitecture(StorageArchitecture):
    """
    Accumulator Architecture (single-address storage hierarchy).
    Operands are implicitly in a single special register (Accumulator).
    """
    has_link_bit: bool

class GeneralRegisterArchitecture(StorageArchitecture):
    """
    General-Purpose Register (GPR) Architecture (double/multi-address storage).
    Operands are stored in a file of addressable registers.
    """
    num_gprs: int
    num_fprs: int

class VectorArchitecture(GeneralRegisterArchitecture):
    """
    Vector Register Architecture.
    Support for vector execution on arrays of data.
    """
    vector_length: int
    num_vec_regs: int
