package br.ufes.passagens.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.function.Consumer;

public class TarefaEnvioServidor implements Runnable {

    private final File arquivoTicket;
    private final Path diretorioServidor;
    private final Consumer<ResultadoEnvio> aoConcluir;

    public TarefaEnvioServidor(File arquivoTicket) {
        this(arquivoTicket, obterDiretorioConfigurado(), resultado -> { });
    }

    public TarefaEnvioServidor(File arquivoTicket, Consumer<ResultadoEnvio> aoConcluir) {
        this(arquivoTicket, obterDiretorioConfigurado(), aoConcluir);
    }

    public TarefaEnvioServidor(File arquivoTicket, Path diretorioServidor,
                               Consumer<ResultadoEnvio> aoConcluir) {
        this.arquivoTicket = Objects.requireNonNull(arquivoTicket);
        this.diretorioServidor = Objects.requireNonNull(diretorioServidor);
        this.aoConcluir = Objects.requireNonNull(aoConcluir);
    }

    @Override
    public void run() {
        try {
            if (!arquivoTicket.isFile()) {
                throw new IOException("O arquivo do e-ticket não existe: " + arquivoTicket);
            }
            Files.createDirectories(diretorioServidor);
            Path destino = diretorioServidor.resolve(arquivoTicket.getName());
            Files.copy(arquivoTicket.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
            aoConcluir.accept(new ResultadoEnvio(true, destino, null));
        } catch (IOException e) {
            aoConcluir.accept(new ResultadoEnvio(false, null, e.getMessage()));
        }
    }

    private static Path obterDiretorioConfigurado() {
        String valor = System.getenv("DJE_SERVER_DIR");
        if (valor == null || valor.isBlank()) {
            return Path.of("output", "servidor");
        }
        return Path.of(valor);
    }

    public record ResultadoEnvio(boolean enviado, Path destino, String erro) { }
}
