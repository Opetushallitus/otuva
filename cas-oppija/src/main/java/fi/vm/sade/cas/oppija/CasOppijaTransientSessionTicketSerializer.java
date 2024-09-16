package fi.vm.sade.cas.oppija;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apereo.cas.ticket.TransientSessionTicketImpl;
import org.apereo.cas.util.serialization.SerializationUtils;
import org.apereo.cas.util.serialization.StringSerializer;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Slf4j
public class CasOppijaTransientSessionTicketSerializer implements StringSerializer<TransientSessionTicketImpl> {
    private static final Charset charset = StandardCharsets.UTF_8;
    private final Base64.Encoder encoder = Base64.getEncoder();
    private final Base64.Decoder decoder = Base64.getDecoder();
    public CasOppijaTransientSessionTicketSerializer() {
        LOGGER.info("Initializing CasOppijaTransientSessionTicketSerializer");
    }

    @Override
    public TransientSessionTicketImpl from(String s) {
        return fromBase64String(s);
    }

    @Override
    public TransientSessionTicketImpl from(Reader reader) {
        try {
            return fromBase64String(IOUtils.toString(reader));
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public TransientSessionTicketImpl from(InputStream stream) {
        try {
            return fromBase64String(IOUtils.toString(stream, charset));
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public TransientSessionTicketImpl from(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            return from(fis);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public TransientSessionTicketImpl from(Writer writer) {
        // Seems like a bug as toString is inherited from java.lang.Object but AbstractJacksonBackedSerializer does the same
        return from(writer.toString());
    }

    private TransientSessionTicketImpl fromBase64String(String s) {
        byte[] bytes = decoder.decode(s.getBytes(StandardCharsets.UTF_8));
        return SerializationUtils.deserialize(bytes, TransientSessionTicketImpl.class);
    }

    @Override
    public void to(OutputStream out, TransientSessionTicketImpl object) {
        try {
            IOUtils.write(toBase64String(object), out, charset);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void to(Writer out, TransientSessionTicketImpl object) {
        try {
            IOUtils.write(toBase64String(object), out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void to(File out, TransientSessionTicketImpl object) {
        try {
            FileUtils.write(out, toBase64String(object), charset);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String toBase64String(TransientSessionTicketImpl object) {
        return encoder.encodeToString(SerializationUtils.serialize(object));
    }

    @Override
    public String toString(TransientSessionTicketImpl object) {
        return toBase64String(object);
    }

    @Override
    public Class<TransientSessionTicketImpl> getTypeToSerialize() {
        return TransientSessionTicketImpl.class;
    }

    @Override
    public List<TransientSessionTicketImpl> fromList(String s) {
        byte[] bytes = decoder.decode(s.getBytes(StandardCharsets.UTF_8));
        return SerializationUtils.deserialize(bytes, List.class);
    }
}
