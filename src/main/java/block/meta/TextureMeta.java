package block.meta;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
        @JsonSubTypes.Type(DefaultTextureMeta.class),
        @JsonSubTypes.Type(AllTextureMeta.class),
        @JsonSubTypes.Type(NoTextureMeta.class)
})
public sealed interface TextureMeta permits DefaultTextureMeta, AllTextureMeta, NoTextureMeta {}
