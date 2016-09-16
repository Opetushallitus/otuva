package fi.vm.sade.kayttooikeus.domain;

import lombok.Getter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "text_group")
public class TextGroup extends IdentifiableAndVersionedEntity {
    @Getter
    @OneToMany(mappedBy = "textGroup", cascade = CascadeType.ALL)
    private Set<Text> texts = new HashSet<Text>();

    public void addText(Text text) {
        text.setTextGroup(this);
        texts.add(text);
    }

    public void removeText(Text text) {
        texts.remove(text);
    }

    public void clearTexts() {
        texts.clear();
    }
}
