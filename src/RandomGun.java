package U_1F47B;

public class RandomGun extends Gun {

    public RandomGun(State state) {
        super(state);
    }

    @Override
    public void execute() {
        this.rotation = Math.random()*360 - 180;
    }

}
