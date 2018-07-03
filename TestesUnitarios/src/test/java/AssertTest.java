import br.ce.wcaquino.entidades.Usuario;
import org.junit.Assert;
import org.junit.Test;

public class AssertTest {

    @Test
    public void teste(){
        Assert.assertTrue(true);
        Assert.assertFalse(false);

        int i = 5;
        Integer i2 = 5;
        Assert.assertEquals(Integer.valueOf(i),i2);
        Assert.assertEquals(i,i2.intValue());

        Assert.assertEquals("bola","bola");
        Assert.assertTrue("bola".equalsIgnoreCase("Bola"));
        Assert.assertTrue("bola".startsWith("bo"));

        Usuario u1 = new Usuario("Usuario 1");
        Usuario u2 = new Usuario("Usuario 1");
        Usuario u3 = null;

        Assert.assertEquals(u1,u2);
        Assert.assertSame(u2, u2);
        Assert.assertNull(u3);

    }
}
