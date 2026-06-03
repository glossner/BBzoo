from abc import ABC
from typing import List

class InstructionFormat(ABC):
    """
    Base class for instruction coding formats.
    """
    op_code_width: int

class FixedLengthFormat(InstructionFormat):
    """
    Fixed-length instruction format.
    All instructions are of the same size.
    """
    instruction_width: int

class VariableLengthFormat(InstructionFormat):
    """
    Variable-length instruction format.
    Instructions can span multiple different lengths depending on the instruction type.
    """
    formats: List[str]
