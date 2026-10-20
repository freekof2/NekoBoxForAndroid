package libcore

import (
	"testing"

	C "github.com/sagernet/sing-box/constant"
	"github.com/sagernet/sing-box/option"
)

func TestAndroidOutboundRegistryRegistersTURN(t *testing.T) {
	options, loaded := nekoboxAndroidOutboundRegistry().CreateOptions(C.TypeTURN)
	if !loaded {
		t.Fatal("TURN outbound type is not registered")
	}
	if _, ok := options.(*option.TURNOutboundOptions); !ok {
		t.Fatalf("unexpected TURN options type: %T", options)
	}
}
