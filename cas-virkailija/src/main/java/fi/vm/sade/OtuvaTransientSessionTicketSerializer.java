package fi.vm.sade;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apereo.cas.ticket.TransientSessionTicketImpl;
import org.apereo.cas.util.serialization.StringSerializer;
import org.apereo.cas.util.serialization.SerializationUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OtuvaTransientSessionTicketSerializer implements StringSerializer<TransientSessionTicketImpl> {
    private static final Charset charset = StandardCharsets.UTF_8;
    private final Base64.Encoder encoder = Base64.getEncoder();
    private final Base64.Decoder decoder = Base64.getDecoder();

    public OtuvaTransientSessionTicketSerializer() {
        LOGGER.info("Initializing OtuvaTransientSessionTicketSerializer");
    }

    @Override
    public TransientSessionTicketImpl from(String s) {
        LOGGER.info("from String");
        return fromBase64String(s);
    }

    @Override
    public TransientSessionTicketImpl from(Reader reader) {
        LOGGER.info("from Reader");
        try {
            return fromBase64String(IOUtils.toString(reader));
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public TransientSessionTicketImpl from(InputStream stream) {
        LOGGER.info("from InputStream");
        try {
            return fromBase64String(IOUtils.toString(stream, charset));
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public TransientSessionTicketImpl from(File file) {
        LOGGER.info("from File");
        try (FileInputStream fis = new FileInputStream(file)) {
            return from(fis);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public TransientSessionTicketImpl from(Writer writer) {
        LOGGER.info("from Writer");
        // Seems like a bug as toString is inherited from java.lang.Object but AbstractJacksonBackedSerializer does the same
        return from(writer.toString());
    }

    private TransientSessionTicketImpl fromBase64String(String s) {
        byte[] bytes = decoder.decode(s.getBytes(StandardCharsets.UTF_8));
        return SerializationUtils.deserialize(bytes, TransientSessionTicketImpl.class);
    }

    @Override
    public void to(OutputStream out, TransientSessionTicketImpl object) {
        LOGGER.info("to OutputStream");
        try {
            IOUtils.write(toBase64String(object), out, charset);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void to(Writer out, TransientSessionTicketImpl object) {
        LOGGER.info("to Writer");
        try {
            IOUtils.write(toBase64String(object), out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void to(File out, TransientSessionTicketImpl object) {
        LOGGER.info("to File");
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
        LOGGER.info("to String");
        return toBase64String(object);
    }

    @Override
    public Class<TransientSessionTicketImpl> getTypeToSerialize() {
        return TransientSessionTicketImpl.class;
    }

    @Override
    public List<TransientSessionTicketImpl> fromList(String s) {
        LOGGER.info("List from String");
        byte[] bytes = decoder.decode(s.getBytes(StandardCharsets.UTF_8));
        return SerializationUtils.deserialize(bytes, List.class);
    }

    @Override
    public String fromList(Collection<TransientSessionTicketImpl> json) {
        LOGGER.info("List to String");
        var list = new ArrayList<>();
        list.addAll(json);
        return encoder.encodeToString(SerializationUtils.serialize(list));
    }

    @Override
    public TransientSessionTicketImpl merge(TransientSessionTicketImpl baseEntity,
            TransientSessionTicketImpl childEntity) {
        LOGGER.info("merge " + baseEntity.getId() + " to " + childEntity.getId());
        return childEntity;
    }
}
