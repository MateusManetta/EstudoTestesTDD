package br.ce.wcaquino.servicos;

import br.ce.wcaquino.builders.LocacaoBuilder;
import br.ce.wcaquino.daos.LocacaoDAO;
import br.ce.wcaquino.daos.LocacaoDAOFake;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import br.ce.wcaquino.utils.DataUtils;
import buildermaster.BuilderMaster;
import org.hamcrest.CoreMatchers;
import org.junit.*;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static br.ce.wcaquino.builders.FilmeBuilder.umFilme;
import static br.ce.wcaquino.builders.FilmeBuilder.umFilmeSemEstoque;
import static br.ce.wcaquino.builders.LocacaoBuilder.*;
import static br.ce.wcaquino.builders.UsuarioBuilder.umUsuario;
import static br.ce.wcaquino.matchers.MatchersProprios.*;
import static br.ce.wcaquino.utils.DataUtils.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class LocacaoServiceTest {

    private LocacaoService service;
    private SPCService spc;
    private LocacaoDAO dao;
    private EmailService email;


    @Rule
    public ErrorCollector error = new ErrorCollector();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp(){
        service = new LocacaoService();
        dao = Mockito.mock(LocacaoDAO.class);
        service.setLocacaoDAO(dao);
        spc = Mockito.mock(SPCService.class);
        service.setSpcService(spc);
        email = Mockito.mock(EmailService.class);
        service.setEmailService(email);
    }



    @Test
    public void deveAlugarFilme() throws Exception {
        Assume.assumeFalse(verificarDiaSemana(new Date(), Calendar.SATURDAY));

        //cenario
        Usuario usuario = umUsuario().agora();
        List<Filme> filmes = Arrays.asList(umFilme().comValor(5.0).agora());

        //acao
        Locacao locacao = null;
        locacao = service.alugarFilme(usuario, filmes);

        //verificacao
        error.checkThat(locacao.getValor(), is(CoreMatchers.equalTo(5.0)));
        error.checkThat(isMesmaData(locacao.getDataLocacao(), new Date()), is(true));
        error.checkThat(locacao.getDataLocacao(), ehHoje());
        error.checkThat(isMesmaData(locacao.getDataRetorno(), obterDataComDiferencaDias(1)), is(true));
        error.checkThat(locacao.getDataRetorno(), ehHojeComDiferencaDias(1));
    }


    @Test(expected = FilmeSemEstoqueException.class)
    public void naoDeveAlugarFilmeSemEstoque() throws Exception {
        //cenario
        Usuario usuario = umUsuario().agora();
        List<Filme> filmes = Arrays.asList( umFilmeSemEstoque().agora());

        //acao
        service.alugarFilme(usuario, filmes);
    }

    @Test
    public void naoDeveAlugarFilmeSemUsuario() throws FilmeSemEstoqueException {
        //cenario
        List<Filme> filmes = Arrays.asList( umFilme().agora());

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
        Usuario usuario = umUsuario().agora();

        exception.expect(LocadoraException.class);
        exception.expectMessage("Filme Vazio");
        //acao
        service.alugarFilme(usuario,null);
//        System.out.println("Forma Nova");

    }

    @Test
    public void deveDevolverNaSegundaAoAlugarNoSabado() throws FilmeSemEstoqueException, LocadoraException {
        Assume.assumeTrue(verificarDiaSemana(new Date(), Calendar.SATURDAY));
        //cenario
        Usuario usuario = umUsuario().agora();
        List<Filme> filmes = Arrays.asList(umFilme().agora());

        //acao
        Locacao retorno = service.alugarFilme(usuario,filmes);

        //verificacao
        assertThat(retorno.getDataRetorno(), caiNumaSegunda());
    }

    @Test
    public void naoDeveAlugarFilmeParaNegativadoSPC() throws FilmeSemEstoqueException {
        //cenario
        Usuario usuario = umUsuario().agora();
        List<Filme> filmes = Arrays.asList(umFilme().agora());

        Mockito.when(spc.possuiNegativacao(Mockito.any(Usuario.class))).thenReturn(true);

        //acao
        try {
            service.alugarFilme(usuario, filmes);
        //verificacao
            Assert.fail();
        } catch (LocadoraException e) {
            Assert.assertThat(e.getMessage(), is("Usuário Negativado"));
        }

        Mockito.verify(spc).possuiNegativacao(usuario);
    }

    @Test
    public void deveEnviarEmailParaLocacoesAtrasadas(){
        //cenario
        Usuario usuario = umUsuario().agora();
        Usuario usuario2 = umUsuario().comNome("Usuario em dia").agora();
        Usuario usuario3 = umUsuario().comNome("Outro atrasado").agora();
        List<Locacao> locacoes = Arrays.asList(
                umLocacao().atrasado().comUsuario(usuario).agora(),
                umLocacao().comUsuario(usuario2).agora(),
                umLocacao().atrasado().comUsuario(usuario3).agora(),
                umLocacao().atrasado().comUsuario(usuario3).agora());

        Mockito.when(dao.obterLocacoesPendentes()).thenReturn(locacoes);

        //acao
        service.notificarAtrasos();


        //verificacao
        Mockito.verify(email, Mockito.times(3)).notificarAtraso(Mockito.any(Usuario.class));
        Mockito.verify(email).notificarAtraso(usuario);
        Mockito.verify(email, Mockito.atLeastOnce()).notificarAtraso(usuario3);
        Mockito.verify(email, Mockito.never()).notificarAtraso(usuario2);
        Mockito.verifyNoMoreInteractions(email);
    }


}
