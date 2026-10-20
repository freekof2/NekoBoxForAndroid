package moe.matsuri.nb4a.proxy.turn;

import androidx.annotation.NonNull;

import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;

import org.jetbrains.annotations.NotNull;

import io.nekohasekai.sagernet.fmt.AbstractBean;
import io.nekohasekai.sagernet.fmt.KryoConverters;

public class TurnBean extends AbstractBean {

    public String username;
    public String password;
    public String sni;
    public Boolean tls;
    public Boolean allowInsecure;

    @Override
    public void initializeDefaultValues() {
        if (serverPort == null) serverPort = 3478;
        super.initializeDefaultValues();
        if (username == null) username = "";
        if (password == null) password = "";
        if (sni == null) sni = "";
        if (tls == null) tls = false;
        if (allowInsecure == null) allowInsecure = false;
    }

    @Override
    public String network() {
        return "tcp,udp";
    }

    @Override
    public void serialize(ByteBufferOutput output) {
        output.writeInt(0);
        super.serialize(output);
        output.writeString(username);
        output.writeString(password);
        output.writeString(sni);
        output.writeBoolean(tls);
        output.writeBoolean(allowInsecure);
    }

    @Override
    public void deserialize(ByteBufferInput input) {
        int version = input.readInt();
        super.deserialize(input);
        username = input.readString();
        password = input.readString();
        sni = input.readString();
        tls = input.readBoolean();
        allowInsecure = input.readBoolean();
    }

    @NotNull
    @Override
    public TurnBean clone() {
        return KryoConverters.deserialize(new TurnBean(), KryoConverters.serialize(this));
    }

    public static final Creator<TurnBean> CREATOR = new CREATOR<TurnBean>() {
        @NonNull
        @Override
        public TurnBean newInstance() {
            return new TurnBean();
        }

        @Override
        public TurnBean[] newArray(int size) {
            return new TurnBean[size];
        }
    };
}
