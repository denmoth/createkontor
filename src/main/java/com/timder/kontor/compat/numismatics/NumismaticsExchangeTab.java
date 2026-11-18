package com.timder.kontor.compat.numismatics;

import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.SimpleBinding;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.game.block.employee.EmployeeDeskContext;
import com.timder.kontor.game.network.S2CActionResult;
import com.timder.kontor.game.network.ToastPayload;
import com.timder.kontor.game.ui.element.*;
import com.timder.kontor.util.ComponentFormatting;
import com.timder.kontor.util.ObservableValue;
import dev.vfyjxf.taffy.style.FlexDirection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.IntFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class NumismaticsExchangeTab {
    public static UIElement create(EmployeeDeskContext context) {
        ObservableValue<Info> info = new ObservableValue<>(new Info(0, 0, 0, false));
        SimpleBinding<Tag> infoBinding = DataBindingBuilder.tagS2C(() -> infoToTag(context))
                .onRemoteSyncReceived(tag -> info.set(tagToInfo(tag)))
                .build();

        ScrollerView content = (ScrollerView) UiContainer.tabScroller().layout(layout -> layout.gapAll(4));
        content.addSyncValue(infoBinding.getSyncValue());

        int[] selectedSpursToMoney = {1};
        int[] selectedSpursBuyBack = {1};
        content.onMessage("c2s_set_spurs_to_money", tag -> selectedSpursToMoney[0] = tag.getInt("Spurs"));
        content.onMessage("c2s_set_spurs_buy_back", tag -> selectedSpursBuyBack[0] = tag.getInt("Spurs"));

        content.onMessage(
                "c2s_exchange_spurs",
                tag -> ToastPayload.send(context.player(), exchange(context.player(), tag.getInt("Spurs"))));

        content.onMessage(
                "c2s_buy_back_spurs",
                tag -> ToastPayload.send(context.player(), buyBack(context.player(), tag.getInt("Spurs"))));

        info.addListener(() -> {
            boolean unavailable = info.get().centsPerSpur < 1;
            UIElement exchange;
            if (unavailable) {
                exchange = UiLabels.error(
                        Component.translatable("ui.createkontor.numismatics.not_available"), Horizontal.CENTER);
            } else {
                exchange = UiLabels.seperatedLabelRow(List.of(
                        UiLabels.primary(
                                Component.translatable(
                                        "ui.createkontor.numismatics.rate",
                                        Money.ofCents(info.get().centsPerSpur()).toString()),
                                Horizontal.CENTER),
                        UiLabels.primary(
                                Component.translatable(
                                        "ui.createkontor.numismatics.balance",
                                        info.get().playerSpurs()),
                                Horizontal.CENTER),
                        UiLabels.primary(
                                Component.translatable(
                                        "ui.createkontor.numismatics.paid_in",
                                        info.get().paidIn()),
                                Horizontal.CENTER)));
            }

            content.clearAllScrollViewChildren();
            content.addScrollViewChildren(UiContainer.headerWithSubtitle(
                    UiLabels.h1(Component.translatable("ui.createkontor.numismatics.title"), Horizontal.CENTER),
                    exchange));
            if (unavailable) {
                return;
            }

            UIElement spursToMoneyArea = new UIElement()
                    .layout(layout ->
                            layout.flexDirection(FlexDirection.COLUMN).gapAll(2).widthPercent(100));
            spursToMoneyArea.addChild(
                    UiLabels.h2(Component.translatable("ui.createkontor.numismatics.spurs_to_money"), Horizontal.LEFT));
            if (info.get().playerSpurs() > 0) {
                int maxSpurs = info.get().playerSpurs();
                long centsPerSpur = info.get().centsPerSpur();
                selectedSpursToMoney[0] = Math.max(1, Math.min(selectedSpursToMoney[0], maxSpurs));

                IntFunction<Component> worth = amount -> Component.translatable(
                        "ui.createkontor.numismatics.amount",
                        amount,
                        ComponentFormatting.moneyColored(NumismaticsExchangeRules.moneyFor(amount, centsPerSpur)));
                Label worthLabel = UiLabels.secondary(worth.apply(selectedSpursToMoney[0]), Horizontal.LEFT);

                UiTextFields.LabeledTextField spursField = UiTextFields.labeled(
                        Component.translatable("ui.createkontor.numismatics.spurs_to_exchange"),
                        Component.translatable("ui.createkontor.numismatics.spurs_to_exchange"),
                        null,
                        String.valueOf(selectedSpursToMoney[0]),
                        text -> {
                            int value;
                            try {
                                value = Integer.parseInt(text);
                            } catch (NumberFormatException e) {
                                value = 1;
                            }
                            selectedSpursToMoney[0] = value;
                            CompoundTag tag = new CompoundTag();
                            tag.putInt("Spurs", value);
                            content.sendMessage("c2s_set_spurs_to_money", tag);
                        });
                spursField.getTextField().setTextValidator(text -> {
                    if (!text.matches("\\d{0,9}")) return false;
                    int value = text.isEmpty() ? 0 : Integer.parseInt(text);
                    if (value > maxSpurs) return false;
                    worthLabel.setText(worth.apply(value));
                    return true;
                });
                Button exchangeButton =
                        UiButtons.secondary(Component.translatable("ui.createkontor.numismatics.exchange"));
                exchangeButton.layout(layout -> layout.height(20));
                exchangeButton.setOnClick(e -> {
                    int value;
                    try {
                        value = Integer.parseInt(
                                spursField.getTextField().getValue().trim());
                    } catch (NumberFormatException ex) {
                        return;
                    }
                    CompoundTag tag = new CompoundTag();
                    tag.putInt("Spurs", value);
                    content.sendMessage("c2s_exchange_spurs", tag);
                });
                spursToMoneyArea.addChildren(spursField, worthLabel, exchangeButton);
            } else {
                spursToMoneyArea.addChild(UiLabels.paragraphError(
                        Component.translatable("ui.createkontor.numismatics.no_spurs_to_money"), Horizontal.LEFT));
            }

            UIElement moneyToSpursArea = new UIElement()
                    .layout(layout ->
                            layout.flexDirection(FlexDirection.COLUMN).gapAll(2).widthPercent(100));
            moneyToSpursArea.addChild(
                    UiLabels.h2(Component.translatable("ui.createkontor.numismatics.money_to_spurs"), Horizontal.LEFT));
            if (info.get().paidIn() > 0) {
                int maxBuyBack = (int) Math.min(Integer.MAX_VALUE, info.get().paidIn());
                long centsPerSpur = info.get().centsPerSpur();
                selectedSpursBuyBack[0] = Math.max(1, Math.min(selectedSpursBuyBack[0], maxBuyBack));

                IntFunction<Component> cost = amount -> Component.translatable(
                        "ui.createkontor.numismatics.amount",
                        amount,
                        ComponentFormatting.moneyColored(NumismaticsExchangeRules.moneyFor(amount, centsPerSpur)));
                Label costLabel = UiLabels.secondary(cost.apply(selectedSpursBuyBack[0]), Horizontal.LEFT);

                UiTextFields.LabeledTextField buyBackField = UiTextFields.labeled(
                        Component.translatable("ui.createkontor.numismatics.spurs_to_buy_back"),
                        Component.translatable("ui.createkontor.numismatics.spurs_to_buy_back"),
                        null,
                        String.valueOf(selectedSpursBuyBack[0]),
                        text -> {
                            int value;
                            try {
                                value = Integer.parseInt(text);
                            } catch (NumberFormatException e) {
                                value = 1;
                            }
                            selectedSpursBuyBack[0] = value;
                            CompoundTag tag = new CompoundTag();
                            tag.putInt("Spurs", value);
                            content.sendMessage("c2s_set_spurs_buy_back", tag);
                        });
                buyBackField.getTextField().setTextValidator(text -> {
                    if (!text.matches("\\d{0,9}")) return false;
                    int value = text.isEmpty() ? 0 : Integer.parseInt(text);
                    if (value > maxBuyBack) return false;
                    costLabel.setText(cost.apply(value));
                    return true;
                });

                Button buyBackButton =
                        UiButtons.secondary(Component.translatable("ui.createkontor.numismatics.buy_back"));
                buyBackButton.layout(layout -> layout.height(20));
                buyBackButton.setOnClick(e -> {
                    int value;
                    try {
                        value = Integer.parseInt(
                                buyBackField.getTextField().getValue().trim());
                    } catch (NumberFormatException ex) {
                        return;
                    }
                    CompoundTag tag = new CompoundTag();
                    tag.putInt("Spurs", value);
                    content.sendMessage("c2s_buy_back_spurs", tag);
                });
                moneyToSpursArea.addChildren(buyBackField, costLabel, buyBackButton);
            } else {
                moneyToSpursArea.addChild(UiLabels.paragraphError(
                        Component.translatable("ui.createkontor.numismatics.nothing_paid_in"), Horizontal.LEFT));
            }

            content.addScrollViewChildren(UiContainer.twoColumns(List.of(spursToMoneyArea), List.of(moneyToSpursArea)));
        });
        info.runListenersOnce();

        return content;
    }

    private static Tag infoToTag(EmployeeDeskContext context) {
        CompoundTag tag = new CompoundTag();
        Company company = context.company();
        if (!NumismaticsExchange.isAvailable() || company == null || !(context.player() instanceof ServerPlayer player))
            return tag;
        tag.putLong(
                "CentsPerSpur",
                Money.fromDollar(NumismaticsConfig.DOLLARS_PER_SPUR.get()).cents());
        tag.putLong("PaidIn", company.exchangedSpurs());
        tag.putInt("PlayerSpurs", NumismaticsAccess.balanceOf(player.getUUID()));
        tag.putBoolean("Owner", company.isOwner(player.getUUID()));
        return tag;
    }

    private record Info(long centsPerSpur, long paidIn, int playerSpurs, boolean owner) {}

    private static Info tagToInfo(Tag tag) {
        CompoundTag t = (CompoundTag) tag;
        return new Info(t.getLong("CentsPerSpur"), t.getLong("PaidIn"), t.getInt("PlayerSpurs"), t.getBoolean("Owner"));
    }

    private static Optional<S2CActionResult> exchange(Player player, int spurs) {
        if (!(player instanceof ServerPlayer serverPlayer)) return S2CActionResult.illegalEnvironment();

        NumismaticsExchange.Result result = NumismaticsExchange.exchangeSpurs(serverPlayer, spurs);
        if (result.status() != NumismaticsExchange.Status.OK) {
            return S2CActionResult.error(Component.translatable("ui.createkontor.numismatics.status."
                    + result.status().name().toLowerCase(Locale.ROOT)));
        }
        return S2CActionResult.success(Component.translatable(
                "ui.createkontor.numismatics.exchanged", spurs, ComponentFormatting.moneyColored(result.credited())));
    }

    private static Optional<S2CActionResult> buyBack(Player player, int spurs) {
        if (!(player instanceof ServerPlayer serverPlayer)) return S2CActionResult.illegalEnvironment();

        NumismaticsExchange.Result result = NumismaticsExchange.getSpursBack(serverPlayer, spurs);
        if (result.status() != NumismaticsExchange.Status.OK) {
            return S2CActionResult.error(Component.translatable("ui.createkontor.numismatics.status."
                    + result.status().name().toLowerCase(Locale.ROOT)));
        }
        return S2CActionResult.success(Component.translatable(
                "ui.createkontor.numismatics.bought_back", spurs, ComponentFormatting.moneyColored(result.credited())));
    }
}
