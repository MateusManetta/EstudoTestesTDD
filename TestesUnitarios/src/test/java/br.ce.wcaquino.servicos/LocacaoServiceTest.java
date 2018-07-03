package br.ce.wcaquino.servicos;

import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import br.ce.wcaquino.servicos.LocacaoService;
import br.ce.wcaquino.utils.DataUtils;
import org.hamcrest.CoreMatchers;
import org.junit.*;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class LocacaoServiceTest {

    private LocacaoService service;

    @Rule
    public ErrorCollector error = new ErrorCollector();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp(){
        service = new LocacaoService();
    }




    @Test
    public void deveAlugarFilme() throws Exception {
        //cenario
        Usuario usuario = new Usuario("Usuario 1");
        List<Filme> filmes = Arrays.asList( new Filme("Filme 1", 1, 5.0));

        //acao
        Locacao locacao = null;
        locacao = service.alugarFilme(usuario, filmes);

        //verificacao
        error.checkThat(locacao.getValor(), is(CoreMatchers.equalTo(5.0)));
        error.checkThat(DataUtils.isMesmaData(locacao.getDataLocacao(), new Date()), is(true));
        error.checkThat(DataUtils.isMesmaData(locacao.getDataRetorno(), DataUtils.obterDataComDiferencaDias(1)), is(true));

    }


    @Test(expected = FilmeSemEstoqueException.class)
    public void deveLancarExcecaoAoAlugarFilmeSemEstoque() throws Exception {
        //cenario
        Usuario usuario = new Usuario("Usuario 1");
        List<Filme> filmes = Arrays.asList( new Filme("Filme 1", 0, 4.0));

        //acao
        service.alugarFilme(usuario, filmes);
    }

    @Test
    public void naoDeveAlugarFilmeSemUsuario() throws FilmeSemEstoqueException {
        //cenario
        List<Filme> filmes = Arrays.asList( new Filme("Filme 1", 2, 5.0));

        //acao
        try {
            service.alugarFilme(null, filmes);
            Assert.fail();
        }
         catch (LocadoraException e) {
            Assert.assertThat(e.getMessage(), is("Usuario Vazio"));
//            System.out.println("Forma Robusta");
        }
    }

    @Test
    public void naoDeveALugarFilmeSemFilme() throws FilmeSemEstoqueException, LocadoraException {
        //cenario
        Usuario usuario = new Usuario("Usuario 1");

        exception.expect(LocadoraException.class);
        exception.expectMessage("Filme Vazio");
        //acao
        service.alugarFilme(usuario,null);
//        System.out.println("Forma Nova");

    }

    @Test
    public void devePagar75PctNoFilme3() throws FilmeSemEstoqueException, LocadoraException {
        //cenario
        Usuario usuario = new Usuario();
        List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 2,4.0),
        new Filme("Filme 2", 2,4.0),
                new Filme("Filme 3", 2,4.0));

        //acao
        Locacao resultado = service.alugarFilme(usuario,filmes);

        //verificacao
        //4+4+3 = 11
        Assert.assertThat(resultado.getValor(), is(11.0));
    }

    @Test
    public void devePagar50PctNoFilme4() throws FilmeSemEstoqueException, LocadoraException {
        //cenario
        Usuario usuario = new Usuario();
        List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 2,4.0),
                new Filme("Filme 2", 2,4.0),
                new Filme("Filme 3", 2,4.0),
                new Filme("Filme 4", 2,4.0));

        //acao
        Locacao resultado = service.alugarFilme(usuario,filmes);

        //verificacao
        //4+4+3+2 = 11
        Assert.assertThat(resultado.getValor(), is(13.0));
    }

    @Test
    public void devePagar25PctNoFilme5() throws FilmeSemEstoqueException, LocadoraException {
        //cenario
        Usuario usuario = new Usuario();
        List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 2,4.0),
                new Filme("Filme 2", 2,4.0),
                new Filme("Filme 3", 2,4.0),
                new Filme("Filme 4", 2,4.0),
                new Filme("Filme 5", 2,4.0));

        //acao
        Locacao resultado = service.alugarFilme(usuario,filmes);

        //verificacao
        //4+4+3+2+1 = 14
        Assert.assertThat(resultado.getValor(), is(14.0));
    }

    @Test
    public void devePagar0PctNoFilme6() throws FilmeSemEstoqueException, LocadoraException {
        //cenario
        Usuario usuario = new Usuario();
        List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 2,4.0),
                new Filme("Filme 2", 2,4.0),
                new Filme("Filme 3", 2,4.0),
                new Filme("Filme 4", 2,4.0),
                new Filme("Filme 5", 2,4.0),
                new Filme("Filme 6", 2,4.0));

        //acao
        Locacao resultado = service.alugarFilme(usuario,filmes);

        //verificacao
        //4+4+3+2+1 = 14
        Assert.assertThat(resultado.getValor(), is(14.0));
    }

}
