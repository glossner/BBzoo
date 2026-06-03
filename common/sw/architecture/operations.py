from abc import ABC

class Operations(ABC):
    """
    Base class for operation execution capabilities.
    """
    pass

class FixedPointOperations(Operations):
    """
    Fixed-point (integer) and logical operations.
    """
    has_multiply_divide: bool

class FloatingPointOperations(Operations):
    """
    Floating-point (real) operations.
    """
    fpu_precision: int

class DecimalOperations(Operations):
    """
    Decimal and character string operations.
    """
    has_decimal_pack: bool

class VectorOperations(Operations):
    """
    Vector execution operations.
    """
    has_chaining: bool
