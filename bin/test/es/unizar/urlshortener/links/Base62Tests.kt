package es.unizar.urlshortener.links

import es.unizar.urlshortener.links.adapters.persistence.Base62
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class Base62Tests {
    @Test
    fun `the sequence start encodes to the minimum length`() {
        assertEquals("1000", Base62.encode(Base62.START))
        assertEquals(Base62.MIN_CODE_LENGTH, Base62.encode(Base62.START).length)
        assertEquals(Base62.MIN_CODE_LENGTH + 1, Base62.encode(Base62.START * Base62.RADIX).length)
    }
}
