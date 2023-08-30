import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Currency {
    RUB(0), USD(840), EUR(978), TRY(949),GEL(981);

    private final int id;


}