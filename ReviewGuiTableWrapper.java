public class ReviewGuiTableWrapper
{
    private String no;
    private String objective;
    private String achievement;

    public ReviewGuiTableWrapper(String no, String objective, String achievement)
    {
        this.no = no;
        this.objective = objective;
        this.achievement = achievement;
    }

    public ReviewGuiTableWrapper(String no, String objective)
    {
        this.no = no;
        this.objective = objective;
    }

    public String getNo() {
        return no;
    }

    public String getAchievement() {
        return achievement;
    }

    public String getObjective() {
        return objective;
    }

    public void setAchievement(String achievement) {
        this.achievement = achievement;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }
}
